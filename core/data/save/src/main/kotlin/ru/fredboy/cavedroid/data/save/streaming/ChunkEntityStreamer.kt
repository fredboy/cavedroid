package ru.fredboy.cavedroid.data.save.streaming

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.abstraction.ChunkBodiesReadyListener
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.store.ChunkListener
import javax.inject.Inject

/**
 * Streams mobs, drops and containers in and out of the live controllers in lockstep with the
 * infinite world's chunks: when a chunk loads its saved entities are reconstructed and registered;
 * when it unloads they are persisted to that chunk and removed from memory (so they no longer pile
 * up in the global controllers). Also initializes generated block entities in both world types.
 */
@GameScope
class ChunkEntityStreamer @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val dropController: DropController,
    private val containerController: ContainerController,
    private val saveDataRepository: SaveDataRepository,
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val mobPhysicsFactory: MobPhysicsFactory,
    private val dropWorldAdapter: DropWorldAdapter,
    private val containerFactory: ContainerFactory,
) : ChunkListener,
    ChunkBodiesReadyListener {

    private var initialized = false
    private val residentChunks = mutableSetOf<Int>()
    private val restoredChunks = mutableSetOf<Int>()

    /**
     * Initializes finite-world containers, or registers for chunk events and loads entities already resident (the spawn
     * region pre-streamed by the store before listeners existed). Must be called once at session start.
     */
    fun initialize() {
        if (initialized) {
            return
        }
        initialized = true
        if (!gameWorld.isInfinite) {
            initializeContainers(0, gameWorld.width)
            return
        }
        gameWorld.addChunkListener(this)
        gameWorld.addChunkBodiesReadyListener(this)
        gameWorld.forEachLoadedChunk {
            onChunkLoaded(it)
            onChunkBodiesReady(it)
        }
    }

    override fun onChunkLoaded(chunkX: Int) {
        residentChunks.add(chunkX)
    }

    override fun onChunkBodiesReady(chunkX: Int) {
        if (chunkX !in residentChunks) return
        restoreChunk(chunkX)
    }

    private fun restoreChunk(chunkX: Int) {
        if (chunkX in restoredChunks) return
        val data = saveDataRepository.loadChunkEntities(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            chunkX = chunkX,
            mobPhysicsFactory = mobPhysicsFactory,
            dropWorldAdapter = dropWorldAdapter,
        )

        data?.mobs?.forEach(mobController::addMob)
        data?.drops?.forEach(dropController::addLoadedDrop)
        data?.containers?.forEach { (coordinates, container) ->
            if (coordinates !in containerController.containerMap) {
                containerController.addContainer(coordinates.x, coordinates.y, coordinates.z, container)
            }
        }
        initializeContainers(chunkX * ChunkGenerator.CHUNK_W, ChunkGenerator.CHUNK_W)
        restoredChunks.add(chunkX)
    }

    private fun initializeContainers(startX: Int, width: Int) {
        for (x in startX until startX + width) {
            for (y in 0 until gameWorld.height) {
                for (layer in Layer.entries) {
                    val block = when (layer) {
                        Layer.FOREGROUND -> gameWorld.getForeMap(x, y)
                        Layer.BACKGROUND -> gameWorld.getBackMap(x, y)
                    } as? Block.Container ?: continue
                    if (containerController.getContainer(x, y, layer.z) != null) continue
                    val seed = gameWorld.generatorConfig.seed xor (x.toLong() * 341873128712L) xor (y.toLong() * 132897987541L)
                    containerController.addContainer(x, y, layer.z, containerFactory.createContainer(block, seed))
                }
            }
        }
    }

    override fun onChunkUnloaded(chunkX: Int) {
        if (chunkX !in residentChunks) return
        persistChunk(chunkX, evict = true)
        residentChunks.remove(chunkX)
        restoredChunks.remove(chunkX)
    }

    /** Persists the entities of every resident chunk without evicting them (used on full save). */
    fun flushAll() {
        if (!gameWorld.isInfinite) {
            return
        }
        gameWorld.forEachLoadedChunk { chunkX -> persistChunk(chunkX, evict = false) }
    }

    private fun persistChunk(chunkX: Int, evict: Boolean) {
        // Saving can precede collision preparation. Read the old snapshot before replacing it,
        // including when an entity has wandered into a chunk that is still being prepared.
        // Restoration and eviction run on the main thread, with no physics step between them.
        restoreChunk(chunkX)
        mobController.flushPendingSpawns { chunkOf(it) == chunkX }
        dropController.flushPendingSpawns { chunkOf(it) == chunkX }
        val mobs = mobController.mobs.filter { chunkOf(it.position.x) == chunkX }
        val drops = dropController.getAllDrop().filter { chunkOf(it.position.x) == chunkX }
        val containers = containerController.containerMap.filterKeys { chunkOfInt(it.x) == chunkX }

        saveDataRepository.saveChunkEntities(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            chunkX = chunkX,
            mobs = mobs,
            drops = drops,
            containers = containers,
        )

        if (!evict) {
            return
        }

        mobs.forEach { mob ->
            mob.dispose()
            mobController.removeMob(mob)
        }
        dropController.removeDrops(drops)
        containers.keys.forEach(containerController::evictContainer)
    }

    private fun chunkOf(x: Float): Int = chunkOfInt(MathUtils.floor(x))

    private fun chunkOfInt(x: Int): Int = Math.floorDiv(x, ChunkGenerator.CHUNK_W)
}
