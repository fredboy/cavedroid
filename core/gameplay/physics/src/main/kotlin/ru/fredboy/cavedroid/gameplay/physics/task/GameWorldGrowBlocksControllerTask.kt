package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.save.model.GrowBlockEntry
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.store.ChunkListener
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction
import java.util.PriorityQueue

@GameScope
class GameWorldGrowBlocksControllerTask(
    private val gameWorld: GameWorld,
    private val growBlockActions: Map<String, @JvmSuppressWildcards IGrowBlockAction>,
    private val saveDataRepository: SaveDataRepository,
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    initialEntries: List<GrowBlockEntry> = emptyList(),
) : BaseGameWorldControllerTask(),
    ChunkListener {

    private val queueLock = Any()

    private var currentTick = 0L

    private val queue = PriorityQueue<Entry>(16, compareBy(Entry::dueTick))

    private val pending = HashMap<Long, Entry>()

    private val onBlockPlacedListener = OnBlockPlacedListener { block, x, y, layer ->
        if (layer != Layer.FOREGROUND) return@OnBlockPlacedListener
        val key = block.params.key
        if (key !in growBlockActions) return@OnBlockPlacedListener
        schedule(x, y, key, randomDelayTicks())
    }

    private val onBlockDestroyedListener = OnBlockDestroyedListener { _, x, y, layer, _, _ ->
        if (layer != Layer.FOREGROUND) return@OnBlockDestroyedListener
        cancel(x, y)
    }

    init {
        gameWorld.addBlockPlacedListener(onBlockPlacedListener)
        gameWorld.addBlockDestroyedListener(onBlockDestroyedListener)

        restore(initialEntries)

        if (gameWorld.isInfinite) {
            gameWorld.addChunkListener(this)
            gameWorld.forEachLoadedChunk(::onChunkLoaded)
        }
    }

    override fun onChunkLoaded(chunkX: Int) {
        restore(
            saveDataRepository.loadChunkGrowBlocks(
                gameDataFolder = applicationContextRepository.getGameDirectory(),
                saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
                chunkX = chunkX,
            ),
        )
    }

    override fun onChunkUnloaded(chunkX: Int) {
        saveChunkGrowBlocks(chunkX, extractChunk(chunkX))
    }

    /** Persists the pending timers of every resident chunk without removing them (used on full save). */
    fun flushChunks() {
        if (!gameWorld.isInfinite) {
            return
        }
        gameWorld.forEachLoadedChunk { chunkX -> saveChunkGrowBlocks(chunkX, snapshotChunk(chunkX)) }
    }

    private fun restore(entries: List<GrowBlockEntry>) {
        if (entries.isEmpty()) {
            return
        }
        synchronized(queueLock) {
            entries.forEach { restored ->
                if (restored.key !in growBlockActions) return@forEach
                val packed = pack(restored.x, restored.y)
                pending.remove(packed)?.let(queue::remove)
                val due = currentTick + restored.remainingTicks.coerceAtLeast(1L)
                val entry = Entry(due, restored.x, restored.y, restored.key)
                queue.offer(entry)
                pending[packed] = entry
            }
        }
    }

    /** Removes and returns the pending timers whose block lies in [chunkX]. */
    private fun extractChunk(chunkX: Int): List<GrowBlockEntry> {
        synchronized(queueLock) {
            val matched = queue.filter { Math.floorDiv(it.x, ChunkGenerator.CHUNK_W) == chunkX }
            matched.forEach { entry ->
                queue.remove(entry)
                pending.remove(pack(entry.x, entry.y))
            }
            return matched.map(::toEntry)
        }
    }

    private fun snapshotChunk(chunkX: Int): List<GrowBlockEntry> {
        synchronized(queueLock) {
            return queue.filter { Math.floorDiv(it.x, ChunkGenerator.CHUNK_W) == chunkX }.map(::toEntry)
        }
    }

    private fun toEntry(entry: Entry): GrowBlockEntry = GrowBlockEntry(
        x = entry.x,
        y = entry.y,
        key = entry.key,
        remainingTicks = (entry.dueTick - currentTick).coerceAtLeast(1L),
    )

    private fun saveChunkGrowBlocks(chunkX: Int, entries: List<GrowBlockEntry>) {
        saveDataRepository.saveChunkGrowBlocks(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            chunkX = chunkX,
            entries = entries,
        )
    }

    private fun schedule(x: Int, y: Int, key: String, delayTicks: Long) {
        val packed = pack(x, y)
        synchronized(queueLock) {
            pending.remove(packed)?.let(queue::remove)
            val entry = Entry(currentTick + delayTicks, x, y, key)
            queue.offer(entry)
            pending[packed] = entry
        }
    }

    private fun cancel(x: Int, y: Int) {
        val packed = pack(x, y)
        synchronized(queueLock) {
            pending.remove(packed)?.let(queue::remove)
        }
    }

    override fun exec() {
        synchronized(queueLock) { currentTick++ }

        while (true) {
            val due = synchronized(queueLock) {
                val head = queue.peek() ?: return@synchronized null
                if (head.dueTick > currentTick) return@synchronized null
                queue.poll()
                pending.remove(pack(head.x, head.y))
                head
            } ?: break

            val action = growBlockActions[due.key] ?: continue
            val grown = try {
                action.grow(due.x, due.y)
            } catch (t: Throwable) {
                logger.e(t) { "grow() threw for ${due.key} at (${due.x}, ${due.y})" }
                false
            }

            if (!grown && gameWorld.getForeMap(due.x, due.y).params.key == due.key) {
                schedule(due.x, due.y, due.key, randomDelayTicks())
            }
        }
    }

    fun snapshot(): List<GrowBlockEntry> {
        synchronized(queueLock) {
            return queue.map { entry ->
                GrowBlockEntry(
                    x = entry.x,
                    y = entry.y,
                    key = entry.key,
                    remainingTicks = (entry.dueTick - currentTick).coerceAtLeast(1L),
                )
            }
        }
    }

    override fun dispose() {
        super.dispose()
        if (gameWorld.isInfinite) {
            gameWorld.removeChunkListener(this)
        }
        gameWorld.removeBlockPlacedListener(onBlockPlacedListener)
        gameWorld.removeBlockDestroyedListener(onBlockDestroyedListener)
    }

    private data class Entry(val dueTick: Long, val x: Int, val y: Int, val key: String)

    companion object {
        private const val TAG = "GameWorldGrowBlocksControllerTask"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        const val GROW_BLOCKS_UPDATE_INTERVAL_SEC = 1.0f

        private const val MIN_DELAY_TICKS = 30L
        private const val MAX_DELAY_TICKS = 180L

        private fun randomDelayTicks(): Long = MathUtils.random(MIN_DELAY_TICKS, MAX_DELAY_TICKS)

        private fun pack(x: Int, y: Int): Long = (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)
    }
}
