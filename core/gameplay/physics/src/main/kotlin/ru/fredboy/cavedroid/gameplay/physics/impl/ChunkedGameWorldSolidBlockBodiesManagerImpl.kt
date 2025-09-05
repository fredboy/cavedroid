package ru.fredboy.cavedroid.gameplay.physics.impl

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.ChunkUserData
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import java.util.LinkedList
import javax.inject.Inject
import kotlin.experimental.or

@GameScope
class ChunkedGameWorldSolidBlockBodiesManagerImpl @Inject constructor(
    private val itemsRepository: ItemsRepository,
) : GameWorldSolidBlockBodiesManager() {

    override fun initialize() {
        for (x in 0..<gameWorld.width step CHUNK_SIZE) {
            for (y in 0..<gameWorld.height step CHUNK_SIZE) {
                createBody(x, y).let { body ->
                    _bodies[x to y] = body
                }
                updateChunk(x, y)
            }
        }
    }

    override fun onBlockPlaced(
        block: Block,
        x: Int,
        y: Int,
        layer: Layer,
    ) {
        if (layer != Layer.FOREGROUND) {
            return
        }

        updateChunk(x, y)
    }

    private fun updateChunk(blockX: Int, blockY: Int) {
        val chunkX1 = blockX - blockX % CHUNK_SIZE
        val chunkY1 = blockY - blockY % CHUNK_SIZE

        _bodies.remove(chunkX1 to chunkY1)?.let { body ->
            world.destroyBody(body)
        }

        val body = createBody(chunkX1, chunkY1)
        _bodies[chunkX1 to chunkY1] = body

        val (clusters, userData) = getChunkData(chunkX1, chunkY1)

        body.apply {
            this.userData = userData

            clusters.forEach { cluster ->
                val vertices = traceOutline(cluster.points)
                    .onEach { vertex ->
                        vertex.x -= position.x
                        vertex.y -= position.y
                    }

                if (vertices.size < 2) {
                    return@forEach
                }

                val shape = ChainShape().apply {
                    this.createChain(vertices.toTypedArray())
                }

                val fixtureDef = FixtureDef().apply {
                    this.shape = shape
                    density = 1f
                    friction = 0f
                    restitution = 0f
                    filter.categoryBits = PhysicsConstants.CATEGORY_BLOCK

                    if (cluster.block.params.castsShadows) {
                        filter.categoryBits = filter.categoryBits or PhysicsConstants.CATEGORY_OPAQUE
                    }
                }

                body.createFixture(fixtureDef).apply {
                    this.userData = cluster.block
                }
                shape.dispose()
            }
        }
    }

    private fun traceOutline(cluster: Set<Pair<Int, Int>>): List<Vector2> {
        val start = cluster.minWith(compareBy({ it.first }, { it.second }))

        val vertices = LinkedList<Pair<Float, Float>>()

        var current = start
        var currentCorner = Corner.TOP_LEFT

        do {
            val block = gameWorld.getForeMap(current.first, current.second)

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
            gameWorld.getForeMap(start.first, start.second).getCorner(start.first, start.second, Corner.TOP_LEFT)
                .let { (x, y) -> Vector2(x, y) },
        )
    }

    private fun neighbourCoordinates(x: Int, y: Int): List<Pair<Int, Int>> {
        val chunkX1 = x - x % CHUNK_SIZE
        val chunkY1 = y - y % CHUNK_SIZE
        val chunkX2 = chunkX1 + CHUNK_SIZE
        val chunkY2 = chunkY1 + CHUNK_SIZE

        val chunkHorizontal = chunkX1 until chunkX2
        val chunkVertical = chunkY1 until chunkY2

        return listOf(
            (x - 1) to y,
            (x + 1) to y,
            x to y - 1,
            x to y + 1,
        ).filter { (x, y) -> x in chunkHorizontal && y in chunkVertical }
    }

    private fun getSolidBlockOrFallback(x: Int, y: Int): Block {
        return gameWorld.getForeMap(x, y).takeIf { it.params.hasCollision }
            ?: itemsRepository.fallbackBlock
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

                val neighbourCoordinates = neighbourCoordinates(x, y)
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

    private fun createBody(x: Int, y: Int): Body {
        val rect = Rectangle(x.toFloat(), y.toFloat(), CHUNK_SIZE.toFloat(), CHUNK_SIZE.toFloat())

        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            rect.getCenter(position)
            fixedRotation = true
        }

        return world.createBody(bodyDef)
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
