package ru.fredboy.cavedroid.gameplay.lighting.bfs

import kotlin.math.max

/**
 * @param originX absolute world x of local column 0. Public methods accept absolute coordinates and
 * translate by this origin, so a windowed grid (infinite worlds) and a full-width grid (finite,
 * looping worlds, [originX] = 0) share the same call sites. Internal BFS works in local space.
 */
class LightGrid(
    val width: Int,
    val height: Int,
    private val originX: Int = 0,
) {
    private val opaque: BooleanArray = BooleanArray(width * height)
    private val emission: ByteArray = ByteArray(width * height)
    private val sky: ByteArray = ByteArray(width * height)
    private val block: ByteArray = ByteArray(width * height)

    private val heightmap: IntArray = IntArray(width) { height }

    private val transientEmitters = HashMap<Long, TransientEmitter>()

    private val addQueue = LongQueue()
    private val removeQueue = LongQueue()

    fun wrapX(x: Int): Int {
        val w = width
        val m = x % w
        return if (m < 0) m + w else m
    }

    /** Translates an absolute world x into an in-range local column. */
    private fun localX(x: Int): Int = wrapX(x - originX)

    fun isOpaqueAt(x: Int, y: Int): Boolean {
        if (y < 0 || y >= height) return false
        return opaque[index(localX(x), y)]
    }

    fun skyAt(x: Int, y: Int): Int {
        if (y < 0 || y >= height) return 0
        return sky[index(localX(x), y)].toInt() and 0xFF
    }

    fun blockAt(x: Int, y: Int): Int {
        if (y < 0 || y >= height) return 0
        return block[index(localX(x), y)].toInt() and 0xFF
    }

    fun isSkyExposed(x: Int, y: Int): Boolean {
        if (y < 0 || y >= height) return false
        return y < heightmap[localX(x)]
    }

    fun effective(x: Int, y: Int, sunBrightness: Float): Float {
        val skyContribution = skyAt(x, y) * sunBrightness
        val blockContribution = blockAt(x, y).toFloat()
        return max(skyContribution, blockContribution) / MAX_LEVEL_F
    }

    fun rebuildAll(
        isOpaque: (Int, Int) -> Boolean,
        blockEmission: (Int, Int) -> Int,
    ) {
        val rebuild = beginRebuild(isOpaque, blockEmission)
        while (!rebuild.step(Long.MAX_VALUE)) Unit
    }

    internal fun beginRebuild(
        isOpaque: (Int, Int) -> Boolean,
        blockEmission: (Int, Int) -> Int,
    ) = Rebuild(isOpaque, blockEmission)

    /** A candidate grid is private until all phases finish; no partially propagated light is exposed. */
    internal inner class Rebuild(
        private val isOpaque: (Int, Int) -> Boolean,
        private val blockEmission: (Int, Int) -> Int,
    ) {
        private var phase = 0
        private var cell = 0

        fun step(deadline: Long): Boolean {
            var work = 0
            while (phase < 5) {
                when (phase) {
                    0 -> {
                        val x = cell / height
                        val y = cell % height
                        val idx = index(x, y)
                        opaque[idx] = isOpaque(x, y)
                        emission[idx] = blockEmission(x, y).coerceIn(0, MAX_LEVEL).toByte()
                        sky[idx] = 0
                        block[idx] = 0
                        if (++cell == width * height) {
                            cell = 0
                            phase++
                        }
                    }
                    1 -> {
                        val x = cell / height
                        val y = cell % height
                        if (y == 0) heightmap[x] = height
                        if (opaque[index(x, y)] && heightmap[x] == height) heightmap[x] = y
                        if (y < heightmap[x]) {
                            sky[index(x, y)] = MAX_LEVEL.toByte()
                            enqueueAdd(addQueue, x, y, MAX_LEVEL)
                        }
                        if (++cell == width * height) {
                            cell = 0
                            phase++
                        }
                    }
                    2 -> if (addQueue.isNotEmpty()) propagateOne(sky) else phase++
                    3 -> {
                        val x = cell / height
                        val y = cell % height
                        val em = emission[index(x, y)].toInt() and 0xFF
                        if (em > 0) {
                            block[index(x, y)] = em.toByte()
                            enqueueAdd(addQueue, x, y, em)
                        }
                        if (++cell == width * height) {
                            // Seed even emitters inside opaque blocks, matching the synchronous grid.
                            for (emitter in transientEmitters.values) {
                                val idx = index(emitter.x, emitter.y)
                                if (emitter.level > (block[idx].toInt() and 0xFF)) {
                                    block[idx] = emitter.level.toByte()
                                    enqueueAdd(addQueue, emitter.x, emitter.y, emitter.level)
                                }
                            }
                            phase++
                        }
                    }
                    4 -> if (addQueue.isNotEmpty()) propagateOne(block) else phase++
                }
                if (++work == 256) {
                    work = 0
                    if (System.nanoTime() >= deadline) return phase == 5
                }
            }
            return true
        }
    }

    internal var transientRevision = 0L
        private set

    internal fun copyTransientEmittersFrom(source: LightGrid) {
        for ((id, emitter) in source.transientEmitters) {
            val absoluteX = emitter.x + source.originX
            if (absoluteX.toLong() in originX.toLong() until originX.toLong() + width) {
                transientEmitters[id] = emitter.copy(x = absoluteX - originX)
            }
        }
    }

    internal fun seedTransientEmitter(id: Long, x: Int, y: Int, level: Int) {
        if (x.toLong() in originX.toLong() until originX.toLong() + width && y in 0 until height && level > 0) {
            transientEmitters[id] = TransientEmitter(x - originX, y, level.coerceAtMost(MAX_LEVEL))
        }
    }

    fun onCellChanged(x: Int, y: Int, newOpaque: Boolean, newEmission: Int) {
        if (y < 0 || y >= height) return
        val cx = localX(x)
        val idx = index(cx, y)
        val cappedEmission = newEmission.coerceIn(0, MAX_LEVEL)

        val oldOpaque = opaque[idx]
        val oldEmission = emission[idx].toInt() and 0xFF
        if (oldOpaque == newOpaque && oldEmission == cappedEmission) return

        opaque[idx] = newOpaque
        emission[idx] = cappedEmission.toByte()

        val cellBlockBefore = block[idx].toInt() and 0xFF
        if (cellBlockBefore > 0) {
            block[idx] = 0
            enqueueRemove(removeQueue, cx, y, cellBlockBefore)
            drainRemoveQueue(block)
        }

        if (newOpaque) {
            val cellSkyBefore = sky[idx].toInt() and 0xFF
            if (cellSkyBefore > 0) {
                sky[idx] = 0
                enqueueRemove(removeQueue, cx, y, cellSkyBefore)
                drainRemoveQueue(sky)
            }
        }

        if (oldOpaque != newOpaque) {
            recomputeSkyForColumn(cx)
        }

        if (cappedEmission > 0) {
            block[idx] = cappedEmission.toByte()
            enqueueAdd(addQueue, cx, y, cappedEmission)
        }

        if (!newOpaque) {
            for ((dx, dy) in NEIGHBOURS) {
                val nx = wrapX(cx + dx)
                val ny = y + dy
                if (ny < 0 || ny >= height) continue
                val neighbourLevel = block[index(nx, ny)].toInt() and 0xFF
                if (neighbourLevel > 1) {
                    enqueueAdd(addQueue, nx, ny, neighbourLevel)
                }
            }
        }
        drainBlockAddQueue()

        retainTransientEmitters()

        if (!newOpaque) {
            for ((dx, dy) in NEIGHBOURS) {
                val nx = wrapX(cx + dx)
                val ny = y + dy
                if (ny < 0 || ny >= height) continue
                val neighbourLevel = sky[index(nx, ny)].toInt() and 0xFF
                if (neighbourLevel > 1) {
                    enqueueAdd(addQueue, nx, ny, neighbourLevel)
                }
            }
            drainSkyAddQueue()
        }
    }

    fun setTransientEmitter(id: Long, x: Int, y: Int, level: Int) {
        if (level <= 0 || y < 0 || y >= height) {
            clearTransientEmitter(id)
            return
        }
        val cx = localX(x)
        val capped = level.coerceAtMost(MAX_LEVEL)
        val existing = transientEmitters[id]
        if (existing != null && existing.x == cx && existing.y == y && existing.level == capped) return
        clearTransientEmitter(id)
        transientRevision++
        transientEmitters[id] = TransientEmitter(cx, y, capped)
        val current = block[index(cx, y)].toInt() and 0xFF
        if (capped > current) {
            block[index(cx, y)] = capped.toByte()
            enqueueAdd(addQueue, cx, y, capped)
            drainBlockAddQueue()
        }
    }

    fun clearTransientEmitter(id: Long) {
        val existing = transientEmitters.remove(id) ?: return
        transientRevision++
        val cellValue = block[index(existing.x, existing.y)].toInt() and 0xFF
        if (cellValue == 0) return
        if ((emission[index(existing.x, existing.y)].toInt() and 0xFF) >= cellValue) return
        block[index(existing.x, existing.y)] = 0
        enqueueRemove(removeQueue, existing.x, existing.y, cellValue)
        drainRemoveQueue(block)
        drainBlockAddQueue()
    }

    private fun retainTransientEmitters() {
        for (emitter in transientEmitters.values) {
            val idx = index(emitter.x, emitter.y)
            if (opaque[idx]) continue
            val current = block[idx].toInt() and 0xFF
            if (current < emitter.level) {
                block[idx] = emitter.level.toByte()
                enqueueAdd(addQueue, emitter.x, emitter.y, emitter.level)
            }
        }
    }

    private fun recomputeSkyForColumn(cx: Int) {
        val oldTop = heightmap[cx]
        var newTop = height
        for (y in 0 until height) {
            if (opaque[index(cx, y)]) {
                newTop = y
                break
            }
        }
        if (newTop == oldTop) return

        if (newTop < oldTop) {
            for (y in newTop until oldTop) {
                val cellValue = sky[index(cx, y)].toInt() and 0xFF
                if (cellValue > 0) {
                    sky[index(cx, y)] = 0
                    enqueueRemove(removeQueue, cx, y, cellValue)
                }
            }
            drainRemoveQueue(sky)
        } else {
            for (y in oldTop until newTop) {
                if (opaque[index(cx, y)]) continue
                sky[index(cx, y)] = MAX_LEVEL.toByte()
                enqueueAdd(addQueue, cx, y, MAX_LEVEL)
            }
        }
        heightmap[cx] = newTop
        drainSkyAddQueue()
    }

    private fun drainBlockAddQueue() = drainAddQueue(block)

    private fun drainSkyAddQueue() = drainAddQueue(sky)

    private fun drainAddQueue(channel: ByteArray) {
        while (addQueue.isNotEmpty()) propagateOne(channel)
    }

    private fun propagateOne(channel: ByteArray) {
        val packed = addQueue.removeFirst()
        val x = unpackX(packed)
        val y = unpackY(packed)
        val level = unpackLevel(packed)
        val sourceLevel = channel[index(x, y)].toInt() and 0xFF
        if (sourceLevel < level) return
        val propagated = level - 1
        if (propagated <= 0) return

        for ((dx, dy) in NEIGHBOURS) {
            val nx = wrapX(x + dx)
            val ny = y + dy
            if (ny < 0 || ny >= height) continue
            if (opaque[index(nx, ny)]) continue
            val current = channel[index(nx, ny)].toInt() and 0xFF
            if (current >= propagated) continue
            channel[index(nx, ny)] = propagated.toByte()
            enqueueAdd(addQueue, nx, ny, propagated)
        }
    }

    private fun drainRemoveQueue(channel: ByteArray) {
        while (removeQueue.isNotEmpty()) {
            val packed = removeQueue.removeFirst()
            val x = unpackX(packed)
            val y = unpackY(packed)
            val oldLevel = unpackLevel(packed)

            for ((dx, dy) in NEIGHBOURS) {
                val nx = wrapX(x + dx)
                val ny = y + dy
                if (ny < 0 || ny >= height) continue
                val current = channel[index(nx, ny)].toInt() and 0xFF
                if (current == 0) continue
                if (current < oldLevel) {
                    channel[index(nx, ny)] = 0
                    enqueueRemove(removeQueue, nx, ny, current)
                } else {
                    enqueueAdd(addQueue, nx, ny, current)
                }
            }
        }
    }

    private fun index(x: Int, y: Int): Int = y * width + x

    private fun enqueueAdd(queue: LongQueue, x: Int, y: Int, level: Int) {
        queue.addLast(pack(x, y, level))
    }

    private fun enqueueRemove(queue: LongQueue, x: Int, y: Int, oldLevel: Int) {
        queue.addLast(pack(x, y, oldLevel))
    }

    private fun pack(x: Int, y: Int, level: Int): Long {
        return (x.toLong() and 0xFFFFFFL) or
            ((y.toLong() and 0xFFFFL) shl 24) or
            ((level.toLong() and 0xFFL) shl 40)
    }

    private fun unpackX(packed: Long): Int = (packed and 0xFFFFFFL).toInt()

    private fun unpackY(packed: Long): Int = ((packed ushr 24) and 0xFFFFL).toInt()

    private fun unpackLevel(packed: Long): Int = ((packed ushr 40) and 0xFFL).toInt()

    private data class TransientEmitter(val x: Int, val y: Int, val level: Int)

    companion object {
        const val MAX_LEVEL = 15
        const val MAX_LEVEL_F = 15f

        private val NEIGHBOURS = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(-1, 0),
            intArrayOf(0, 1),
            intArrayOf(0, -1),
        )
    }
}
