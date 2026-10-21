package ru.fredboy.cavedroid.gameplay.physics.impl

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.ChunkUserData

/** Own one mesher per worker. Scratch is reused, but returned geometry never aliases it. */
internal class ChunkCollisionMesher(private val fallback: Block) {

    private val labels = IntArray(CELL_COUNT)
    private val queue = IntArray(CELL_COUNT)
    private val normalized = Array(CELL_COUNT) { fallback }
    private val orderedFixtures = arrayOfNulls<CollisionFixture>(CELL_COUNT)
    private val vertices = FloatArray((CELL_COUNT * 4 + 1) * 2)

    fun prepare(snapshot: CollisionSnapshot, xOffset: Int = 0): CollisionSection {
        require(snapshot.blocks.size == CELL_COUNT)
        labels.fill(0)
        orderedFixtures.fill(null)
        val blocks = HashMap<Pair<Int, Int>, Block>(CELL_COUNT * 4 / 3 + 1)
        for (i in 0 until CELL_COUNT) {
            val block = snapshot.blocks[i].takeIf { it.params.hasCollision } ?: fallback
            normalized[i] = block
            blocks[snapshot.x + i / SIZE to snapshot.y + i % SIZE] = block
        }

        var label = 0
        for (start in 0 until CELL_COUNT) {
            if (labels[start] != 0) continue
            label++
            var head = 0
            var tail = 1
            var last = start
            var touchesBoundary = false
            val block = normalized[start]
            queue[0] = start
            labels[start] = label
            while (head < tail) {
                val cell = queue[head++]
                val x = cell / SIZE
                val y = cell % SIZE
                last = maxOf(last, cell)
                touchesBoundary = touchesBoundary || x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1
                if (x > 0) tail = enqueue(cell - SIZE, label, block, tail)
                if (x < SIZE - 1) tail = enqueue(cell + SIZE, label, block, tail)
                if (y > 0) tail = enqueue(cell - 1, label, block, tail)
                if (y < SIZE - 1) tail = enqueue(cell + 1, label, block, tail)
            }

            // Enclosed air contours provide cave boundaries; only boundary-connected air is omitted.
            if (!block.params.hasCollision && touchesBoundary) continue
            orderedFixtures[last] = CollisionFixture(block, trace(snapshot, start, label, xOffset))
        }

        return CollisionSection(
            fixtures = orderedFixtures.filterNotNull(),
            userData = ChunkUserData(snapshot.x until snapshot.x + SIZE, snapshot.y until snapshot.y + SIZE, blocks),
        )
    }

    private fun enqueue(cell: Int, label: Int, block: Block, tail: Int): Int {
        if (labels[cell] != 0 || normalized[cell] != block) return tail
        labels[cell] = label
        queue[tail] = cell
        return tail + 1
    }

    private fun trace(snapshot: CollisionSnapshot, start: Int, label: Int, xOffset: Int): FloatArray {
        var cell = start
        var corner = 0
        var count = 0
        do {
            val x = cell / SIZE
            val y = cell % SIZE
            val margins = snapshot.blocks[cell].params.collisionMargins
            val worldX = (snapshot.x + x).toFloat()
            val worldY = (snapshot.y + y).toFloat()
            val vx = if (corner == 0 || corner == 3) worldX + margins.left else worldX + 1f - margins.right
            val vy = if (corner == 0 || corner == 1) worldY + margins.top else worldY + 1f - margins.bottom
            if (count == 0 || vx.toBits() != vertices[count - 2].toBits() || vy.toBits() != vertices[count - 1].toBits()) {
                vertices[count++] = vx
                vertices[count++] = vy
            }

            val next = when (corner) {
                0 -> if (y > 0) cell - 1 else -1
                1 -> if (x < SIZE - 1) cell + SIZE else -1
                2 -> if (y < SIZE - 1) cell + 1 else -1
                else -> if (x > 0) cell - SIZE else -1
            }
            if (next >= 0 && labels[next] == label) {
                cell = next
                corner = (corner + 3) % 4
            } else {
                corner = (corner + 1) % 4
            }
        } while (cell != start || corner != 0)

        vertices[count++] = vertices[0]
        vertices[count++] = vertices[1]
        val result = vertices.copyOf(count)
        val bodyX = (snapshot.x + xOffset).toFloat() + SIZE / 2f
        val bodyY = snapshot.y.toFloat() + SIZE / 2f
        for (i in result.indices step 2) {
            // Keep the original world-corner/offset/subtraction order, including finite mirrors.
            result[i] = (result[i] + xOffset.toFloat()) - bodyX
            result[i + 1] -= bodyY
        }
        return result
    }

    companion object {
        const val SIZE = 16
        private const val CELL_COUNT = SIZE * SIZE
    }
}

/** Main-thread copy in x-major order. Workers only read these references and immutable block params. */
internal class CollisionSnapshot(val x: Int, val y: Int, val blocks: Array<Block>)

internal class CollisionSection(val fixtures: List<CollisionFixture>, val userData: ChunkUserData)

internal class CollisionFixture(val block: Block, val vertices: FloatArray)
