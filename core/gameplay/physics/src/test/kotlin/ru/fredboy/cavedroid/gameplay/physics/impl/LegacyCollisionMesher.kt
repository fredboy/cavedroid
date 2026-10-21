package ru.fredboy.cavedroid.gameplay.physics.impl

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.utils.neighbourCoordinates
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.ChunkUserData
import java.util.LinkedList

/** Frozen pre-optimization algorithm: compare contour ordering and float rounding, not just shapes. */
internal class LegacyCollisionMesher(private val fallback: Block) {

    private lateinit var snapshot: CollisionSnapshot

    fun prepare(snapshot: CollisionSnapshot, offset: Int = 0): CollisionSection {
        this.snapshot = snapshot
        val chunk = getChunkData(snapshot.x, snapshot.y)
        val bodyX = (snapshot.x + offset).toFloat() + 8f
        val bodyY = snapshot.y.toFloat() + 8f
        return CollisionSection(
            chunk.clusters.map { cluster ->
                val points = traceOutline(cluster.points)
                val vertices = FloatArray(points.size * 2)
                points.forEachIndexed { index, point ->
                    vertices[index * 2] = (point.x + offset.toFloat()) - bodyX
                    vertices[index * 2 + 1] = point.y - bodyY
                }
                CollisionFixture(cluster.block, vertices)
            },
            chunk.userData,
        )
    }

    private fun getBlock(x: Int, y: Int): Block = snapshot.blocks[(x - snapshot.x) * 16 + y - snapshot.y]

    private fun traceOutline(cluster: Set<Pair<Int, Int>>): List<Vector2> {
        val start = cluster.minWith(compareBy({ it.first }, { it.second }))

        val vertices = LinkedList<Pair<Float, Float>>()

        var current = start
        var currentCorner = Corner.TOP_LEFT

        do {
            val block = getBlock(current.first, current.second)

            val corner = block.getCorner(current.first, current.second, currentCorner)

            if (vertices.isEmpty() || corner != vertices.last()) {
                vertices.add(corner)
            }

            val next = when (currentCorner) {
                Corner.TOP_LEFT -> Pair(current.first, current.second - 1)
                Corner.TOP_RIGHT -> Pair(current.first + 1, current.second)
                Corner.BOTTOM_RIGHT -> Pair(current.first, current.second + 1)
                Corner.BOTTOM_LEFT -> Pair(current.first - 1, current.second)
            }

            if (next in cluster) {
                current = next
                currentCorner = when (currentCorner) {
                    Corner.TOP_LEFT -> Corner.BOTTOM_LEFT
                    Corner.TOP_RIGHT -> Corner.TOP_LEFT
                    Corner.BOTTOM_RIGHT -> Corner.TOP_RIGHT
                    Corner.BOTTOM_LEFT -> Corner.BOTTOM_RIGHT
                }
            } else {
                currentCorner = when (currentCorner) {
                    Corner.TOP_LEFT -> Corner.TOP_RIGHT
                    Corner.TOP_RIGHT -> Corner.BOTTOM_RIGHT
                    Corner.BOTTOM_RIGHT -> Corner.BOTTOM_LEFT
                    Corner.BOTTOM_LEFT -> Corner.TOP_LEFT
                }
            }
        } while (current != start || currentCorner != Corner.TOP_LEFT)

        return vertices.map { (x, y) -> Vector2(x, y) } + listOf(
            getBlock(start.first, start.second).getCorner(start.first, start.second, Corner.TOP_LEFT)
                .let { (x, y) -> Vector2(x, y) },
        )
    }

    private fun getSolidBlockOrFallback(x: Int, y: Int): Block {
        return getBlock(x, y).takeIf { it.params.hasCollision }
            ?: fallback
    }

    private fun getChunkData(chunkX: Int, chunkY: Int): Chunk {
        val clusters = mutableListOf<Cluster>()
        val blocksMap = mutableMapOf<Pair<Int, Int>, Block>()

        val boundX = chunkX until chunkX + CHUNK_SIZE
        val boundY = chunkY until chunkY + CHUNK_SIZE

        for (x in boundX) {
            for (y in boundY) {
                val block = getSolidBlockOrFallback(x, y)
                blocksMap[x to y] = block

                val neighbourCoordinates = neighbourCoordinates(x, y, CHUNK_SIZE)
                    .filter { getSolidBlockOrFallback(it.first, it.second) == block }
                val neighbourClusters = clusters.filter { cluster ->
                    neighbourCoordinates.any { it in cluster.points }
                }
                clusters.removeAll(neighbourClusters)

                val merged = neighbourClusters
                    .map(Cluster::points)
                    .flatten()
                    .takeIf { it.isNotEmpty() }
                    ?.toMutableSet()
                    ?: mutableSetOf()

                merged.add(x to y)
                clusters.add(
                    Cluster(
                        points = merged,
                        block = block,
                    ),
                )
            }
        }

        return Chunk(
            clusters = clusters.filter { cluster ->
                cluster.points.none { (x, y) ->
                    !cluster.block.params.hasCollision &&
                        (x == boundX.first || x == boundX.last || y == boundY.first || y == boundY.last)
                }
            },
            userData = ChunkUserData(
                boundX = boundX,
                boundY = boundY,
                blocks = blocksMap,
            ),
        )
    }

    private fun Block.getCorner(x: Int, y: Int, corner: Corner): Pair<Float, Float> {
        return when (corner) {
            Corner.TOP_LEFT -> Pair(
                x.toFloat() + params.collisionMargins.left,
                y.toFloat() + params.collisionMargins.top,
            )

            Corner.TOP_RIGHT -> Pair(
                x.toFloat() + 1f - params.collisionMargins.right,
                y.toFloat() + params.collisionMargins.top,
            )

            Corner.BOTTOM_RIGHT -> Pair(
                x.toFloat() + 1f - params.collisionMargins.right,
                y.toFloat() + 1f - params.collisionMargins.bottom,
            )

            Corner.BOTTOM_LEFT -> Pair(
                x.toFloat() + params.collisionMargins.left,
                y.toFloat() + 1f - params.collisionMargins.bottom,
            )
        }
    }

    private data class Chunk(
        val clusters: List<Cluster>,
        val userData: ChunkUserData,
    )

    private data class Cluster(
        val points: Set<Pair<Int, Int>>,
        val block: Block,
    )

    private enum class Corner {
        TOP_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
        TOP_RIGHT,
    }

    companion object {
        private const val CHUNK_SIZE = 16
    }
}
