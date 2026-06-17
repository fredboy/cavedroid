package ru.fredboy.cavedroid.data.save.streaming

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
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
 * up in the global controllers). No-op for finite, looping worlds.
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
) : ChunkListener,
    ChunkBodiesReadyListener {

    private var initialized = false

    /**
     * Registers for chunk events and loads the entities of the chunks already resident (the spawn
     * region pre-streamed by the store before listeners existed). Must be called once at session start.
     */
    fun initialize() {
        if (!gameWorld.isInfinite || initialized) {
            return
        }
        initialized = true
        gameWorld.addChunkListener(this)
        gameWorld.addChunkBodiesReadyListener(this)
        gameWorld.forEachLoadedChunk(::onChunkBodiesReady)
    }

    override fun onChunkLoaded(chunkX: Int) = Unit

    override fun onChunkBodiesReady(chunkX: Int) {
        val data = saveDataRepository.loadChunkEntities(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            chunkX = chunkX,
            mobPhysicsFactory = mobPhysicsFactory,
            dropWorldAdapter = dropWorldAdapter,
        ) ?: return

        data.mobs.forEach(mobController::addMob)
        data.drops.forEach(dropController::addLoadedDrop)
        data.containers.forEach { (coordinates, container) ->
            containerController.addContainer(coordinates.x, coordinates.y, coordinates.z, container)
        }
    }

    override fun onChunkUnloaded(chunkX: Int) {
        persistChunk(chunkX, evict = true)
    }

    /** Persists the entities of every resident chunk without evicting them (used on full save). */
    fun flushAll() {
        if (!gameWorld.isInfinite) {
            return
        }
        gameWorld.forEachLoadedChunk { chunkX -> persistChunk(chunkX, evict = false) }
    }

    private fun persistChunk(chunkX: Int, evict: Boolean) {
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
