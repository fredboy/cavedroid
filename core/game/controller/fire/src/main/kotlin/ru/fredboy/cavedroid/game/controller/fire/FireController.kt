package ru.fredboy.cavedroid.game.controller.fire

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.save.model.FireEntry
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.game.world.store.ChunkListener
import javax.inject.Inject

@GameScope
class FireController @Inject constructor(
    private val gameWorld: GameWorld,
    private val lightingSystem: LightingSystem,
    private val saveDataRepository: SaveDataRepository,
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
) : OnBlockDestroyedListener,
    ChunkListener,
    Disposable {

    data class FireInstance(
        val x: Int,
        val y: Int,
        val layer: Layer,
        var age: Float = 0f,
        var lightHandle: LightHandle? = null,
    )

    private val fires = mutableMapOf<Long, FireInstance>()

    val size: Int get() = fires.size

    init {
        gameWorld.addBlockDestroyedListener(this)
        if (gameWorld.isInfinite) {
            gameWorld.addChunkListener(this)
            gameWorld.forEachLoadedChunk(::onChunkLoaded)
        }
    }

    private fun key(x: Int, y: Int, layer: Layer): Long {
        val wrappedX = if (!gameWorld.isInfinite) {
            ((x % gameWorld.width) + gameWorld.width) % gameWorld.width
        } else {
            x
        }

        // 16 bits per axis is plenty for any sane world, and `layer.ordinal`
        // fits in the high byte without overlapping any of them.
        return (layer.ordinal.toLong() shl 48) or
            (wrappedX.toLong() and 0xFFFFL shl 32) or
            (y.toLong() and 0xFFFFFFFFL)
    }

    fun getFireAt(x: Int, y: Int, layer: Layer): FireInstance? = fires[key(x, y, layer)]

    fun hasFireAt(x: Int, y: Int, layer: Layer): Boolean = fires.containsKey(key(x, y, layer))

    fun hasAnyFireAt(x: Int, y: Int): Boolean = Layer.entries.any { hasFireAt(x, y, it) }

    /**
     * Ignites the block at [x], [y] on [layer] when it is combustible and not
     * already on fire. Returns the new [FireInstance], or null when nothing
     * happened.
     */
    fun addFire(x: Int, y: Int, layer: Layer): FireInstance? {
        if (hasFireAt(x, y, layer)) return null
        val support = blockAt(x, y, layer)
        if (!FireSpreadRules.supportStillValid(support)) return null

        val instance = FireInstance(
            x = x,
            y = y,
            layer = layer,
            age = 0f,
            lightHandle = lightingSystem.createFireLight(x + 0.5f, y + 0.5f),
        )
        fires[key(x, y, layer)] = instance
        return instance
    }

    /**
     * Convenience for "ignite whatever combustible is at this cell": prefers
     * foreground, falls back to background. Returns the fire instance, or null
     * if neither layer is combustible.
     */
    fun ignite(x: Int, y: Int): FireInstance? = addFire(x, y, Layer.FOREGROUND) ?: addFire(x, y, Layer.BACKGROUND)

    fun removeFire(x: Int, y: Int, layer: Layer) {
        val instance = fires.remove(key(x, y, layer)) ?: return
        instance.lightHandle?.dispose()
    }

    private fun blockAt(x: Int, y: Int, layer: Layer) = when (layer) {
        Layer.FOREGROUND -> gameWorld.getForeMap(x, y)
        Layer.BACKGROUND -> gameWorld.getBackMap(x, y)
    }

    /**
     * Snapshot of every active fire — safe to mutate the controller from the
     * caller (e.g., the logic task wraps mutations in a main-thread runnable).
     */
    fun snapshot(): List<FireInstance> = fires.values.toList()

    override fun onBlockDestroyed(
        block: Block,
        x: Int,
        y: Int,
        layer: Layer,
        withDrop: Boolean,
        destroyedByPlayer: Boolean,
    ) {
        removeFire(x, y, layer)
    }

    override fun onChunkLoaded(chunkX: Int) {
        saveDataRepository.loadChunkFire(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            chunkX = chunkX,
        ).forEach { entry ->
            addFire(entry.x, entry.y, entry.layer)?.age = entry.age
        }
    }

    override fun onChunkUnloaded(chunkX: Int) {
        val entries = chunkFireEntries(chunkX)
        saveChunkFire(chunkX, entries)
        entries.forEach { removeFire(it.x, it.y, it.layer) }
    }

    /** Persists the fire of every resident chunk without removing it (used on full save). */
    fun flushChunks() {
        if (!gameWorld.isInfinite) {
            return
        }
        gameWorld.forEachLoadedChunk { chunkX -> saveChunkFire(chunkX, chunkFireEntries(chunkX)) }
    }

    private fun chunkFireEntries(chunkX: Int): List<FireEntry> = fires.values
        .filter { Math.floorDiv(it.x, ChunkGenerator.CHUNK_W) == chunkX }
        .map { FireEntry(x = it.x, y = it.y, layer = it.layer, age = it.age) }

    private fun saveChunkFire(chunkX: Int, entries: List<FireEntry>) {
        saveDataRepository.saveChunkFire(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            chunkX = chunkX,
            entries = entries,
        )
    }

    override fun dispose() {
        if (gameWorld.isInfinite) {
            gameWorld.removeChunkListener(this)
        }
        fires.values.forEach { it.lightHandle?.dispose() }
        fires.clear()
        gameWorld.removeBlockDestroyedListener(this)
    }
}
