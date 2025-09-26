package ru.fredboy.cavedroid.game.world

import box2dLight.DirectionalLight
import box2dLight.Light
import box2dLight.PointLight
import box2dLight.RayHandler
import box2dLight.publicUpdate
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Filter
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.neighbourCoordinates
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject
import kotlin.math.max

@GameScope
class GameWorldLightManager @Inject constructor(
    private val gameContextRepository: GameContextRepository,
) : OnBlockPlacedListener,
    Disposable {

    private var _gameWorld: GameWorld? = null

    private var _rayHandler: RayHandler? = null

    private var _sunLight: DirectionalLight? = null

    private var updateAccumulator = 0f

    private val gameWorld: GameWorld
        get() = requireNotNull(_gameWorld)

    private val sunLight: DirectionalLight
        get() = requireNotNull(_sunLight)

    val rayHandler: RayHandler
        get() = requireNotNull(_rayHandler)

    private val blockLights = mutableMapOf<Pair<Int, Int>, List<Light>>()

    fun attachToGameWorld(gameWorld: GameWorld) {
        if (_gameWorld != null) {
            logger.w { "GameWorldLightManager already attached" }
            return
        }

        _gameWorld = gameWorld

        _rayHandler = RayHandler(gameWorld.world)

        _sunLight = DirectionalLight(rayHandler, 64, Color().apply { a = 1f }, 90.1f).apply {
            val filter = Filter().apply {
                maskBits = PhysicsConstants.CATEGORY_OPAQUE
            }
            setContactFilter(filter)
            setSoftnessLength(5f)
        }

        for (x in 0..<gameWorld.width step CHUNK_SIZE) {
            for (y in 0..<gameWorld.height step CHUNK_SIZE) {
                updateChunk(x, y)
            }
        }

        gameWorld.addBlockPlacedListener(this)
    }

    override fun onBlockPlaced(block: Block, x: Int, y: Int, layer: Layer) {
        updateChunk(x, y)
        updateVisibleLights()
    }

    override fun dispose() {
        gameWorld.removeBlockPlacedListener(this)
        _gameWorld = null

        rayHandler.dispose()
        _rayHandler = null
        _sunLight = null
        blockLights.clear()
    }

    private fun updateVisibleLights() {
        gameContextRepository.getCameraContext().visibleWorld.let { visibleWorld ->
            blockLights.asSequence()
                .flatMap { it.value }
                .filter { visibleWorld.contains(it.position) }
                .forEach { it.publicUpdate() }
        }
    }

    fun isMobExposedToSun(mob: Mob): Boolean {
        return sunLight.contains(mob.position.x, mob.position.y)
    }

    fun update(delta: Float) {
        updateAccumulator += delta

        if (updateAccumulator < SUN_UPDATE_FREQUENCY) {
            return
        }

        updateAccumulator = 0f

        var sunAngle = (gameWorld.getNormalizedTime() * 30f + 75f)
        if (sunAngle >= 120f) sunAngle -= 60f
        if (MathUtils.isEqual(90f, sunAngle, 0.1f)) {
            sunAngle = 90.1f
        }
        sunLight.direction = sunAngle
        sunLight.color = Color().apply { a = max(gameWorld.getSunlight(), 0.1f) }
        sunLight.publicUpdate()
    }

    private fun updateChunk(blockX: Int, blockY: Int) {
        val chunkX1 = blockX - blockX % CHUNK_SIZE
        val chunkY1 = blockY - blockY % CHUNK_SIZE

        blockLights.remove(chunkX1 to chunkY1)?.let { lights ->
            lights.forEach { light ->
                light.remove(true)
            }
        }

        val clusters = getChunkData(chunkX1, chunkY1).clusters

        blockLights[chunkX1 to chunkY1] = clusters.map { cluster ->
            val lightInfo = cluster.block.params.lightInfo ?: return

            val lightPosition = cluster.getLightPoint()

            PointLight(
                rayHandler,
                128,
                Color().apply { a = lightInfo.lightBrightness },
                lightInfo.lightDistance,
                lightPosition.x,
                lightPosition.y,
            ).apply {
                val filter = Filter().apply {
                    maskBits = PhysicsConstants.CATEGORY_OPAQUE
                }
                setContactFilter(filter)
                setSoftnessLength(3f)
                isStaticLight = true
            }
        }
    }

    private fun Cluster.getLightPoint(): Vector2 {
        val pointsSequence = points.asSequence()

        val avgX = pointsSequence.map { it.first }.average()
        val avgY = pointsSequence.map { it.second }.average()

        val best = pointsSequence.minBy { (x, y) ->
            val dx = x - avgX
            val dy = y - avgY
            dx * dx + dy * dy
        }

        return block.getRectangle(best.first, best.second).getCenter(Vector2())
    }

    private fun getLightSourceBlock(x: Int, y: Int): Block? {
        return gameWorld.getForeMap(x, y).takeIf { it.params.lightInfo != null }
            ?: gameWorld.getBackMap(x, y).takeIf { it.params.lightInfo != null }
    }

    private fun getChunkData(chunkX: Int, chunkY: Int): Chunk {
        val clusters = mutableListOf<Cluster>()
        val blocksMap = mutableMapOf<Pair<Int, Int>, Block>()

        val boundX = chunkX until chunkX + CHUNK_SIZE
        val boundY = chunkY until chunkY + CHUNK_SIZE

        for (x in boundX) {
            for (y in boundY) {
                val block = getLightSourceBlock(x, y) ?: continue

                blocksMap[x to y] = block

                val neighbourCoordinates = neighbourCoordinates(x, y, CHUNK_SIZE)
                    .filter { getLightSourceBlock(it.first, it.second)?.params?.lightInfo != null }
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

        return Chunk(clusters)
    }

    private data class Chunk(
        val clusters: List<Cluster>,
    )

    private data class Cluster(
        val points: Set<Pair<Int, Int>>,
        val block: Block,
    )

    companion object {
        private const val TAG = "GameWorldLightManager"
private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val CHUNK_SIZE = 4

        private const val SUN_UPDATE_FREQUENCY = 1f / 10f
    }
}
