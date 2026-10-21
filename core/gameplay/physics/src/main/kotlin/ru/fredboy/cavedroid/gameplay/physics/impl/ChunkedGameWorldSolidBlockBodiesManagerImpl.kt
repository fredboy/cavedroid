package ru.fredboy.cavedroid.gameplay.physics.impl

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainThread
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.effectiveMirrorBand
import ru.fredboy.cavedroid.common.utils.floorDiv
import ru.fredboy.cavedroid.common.utils.floorToInt
import ru.fredboy.cavedroid.common.utils.mirrorSidesFor
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.store.ChunkListener
import javax.inject.Inject
import kotlin.experimental.or

@GameScope
class ChunkedGameWorldSolidBlockBodiesManagerImpl @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val appDispatchers: AppDispatchers,
    private val gameContextRepository: GameContextRepository,
) : GameWorldSolidBlockBodiesManager(),
    ChunkListener {

    private val coroutineScope = CoroutineScope(Job() + appDispatchers.main)
    private val mirrorBodies = mutableMapOf<Triple<Int, Int, Int>, Body>()
    private val synchronousMesher by lazy { ChunkCollisionMesher(itemsRepository.fallbackBlock) }
    private val preparation by lazy {
        ChunkBodyPreparation(
            scope = coroutineScope,
            background = appDispatchers.background,
            sectionCount = (gameWorld.height + CHUNK_SIZE - 1) / CHUNK_SIZE,
            snapshot = { chunkX: Int, section: Int -> snapshot(chunkX * ChunkGenerator.CHUNK_W, section * CHUNK_SIZE) },
            createBuilder = {
                val mesher = ChunkCollisionMesher(itemsRepository.fallbackBlock)
                val build: (CollisionSnapshot) -> CollisionSection = { mesher.prepare(it) }
                build
            },
            install = { chunkX, section, prepared -> install(chunkX * ChunkGenerator.CHUNK_W, section * CHUNK_SIZE, prepared) },
            onReady = ::notifyChunkBodiesReady,
        )
    }

    override fun initialize() {
        if (gameWorld.isInfinite) {
            gameWorld.addChunkListener(this)
            gameWorld.forEachLoadedChunk { preparation.load(it, synchronous = true) }
            return
        }
        for (x in 0 until gameWorld.width step CHUNK_SIZE) {
            for (y in 0 until gameWorld.height step CHUNK_SIZE) updateChunk(x, y)
        }
    }

    override fun onChunkLoaded(chunkX: Int) {
        preparation.load(chunkX)
    }

    override fun onChunkUnloaded(chunkX: Int) {
        preparation.unload(chunkX)
        val startX = chunkX * ChunkGenerator.CHUNK_W
        for (y in 0 until gameWorld.height step CHUNK_SIZE) {
            _bodies.remove(startX to y)?.let(world::destroyBody)
        }
    }

    override fun prepareForStep() {
        if (!gameWorld.isInfinite) return
        val visible = gameContextRepository.getCameraContext().visibleWorld
        // A one-block margin includes bodies that can overlap the viewport edge.
        val first = (visible.x.floorToInt() - 1) floorDiv ChunkGenerator.CHUNK_W
        val last = ((visible.x + visible.width).floorToInt() + 1) floorDiv ChunkGenerator.CHUNK_W
        for (chunkX in first..last) preparation.finish(chunkX)
    }

    override fun onBlockPlaced(block: Block, x: Int, y: Int, layer: Layer) {
        if (layer != Layer.FOREGROUND || y !in 0 until gameWorld.height) return
        if (gameWorld.isInfinite) {
            preparation.rebuild(x floorDiv ChunkGenerator.CHUNK_W, y / CHUNK_SIZE)
        } else {
            updateChunk(x, y)
        }
    }

    override fun dispose() {
        coroutineScope.cancel()
        if (gameWorld.isInfinite) preparation.clear()
        gameWorld.removeChunkListener(this)
        mirrorBodies.values.forEach { it.world.destroyBody(it) }
        mirrorBodies.clear()
        super.dispose()
    }

    private fun snapshot(x: Int, y: Int): CollisionSnapshot = CollisionSnapshot(
        x = x,
        y = y,
        blocks = Array(CHUNK_SIZE * CHUNK_SIZE) { i -> gameWorld.getForeMap(x + i / CHUNK_SIZE, y + i % CHUNK_SIZE) },
    )

    private fun updateChunk(blockX: Int, blockY: Int) {
        val x = (blockX floorDiv CHUNK_SIZE) * CHUNK_SIZE
        val y = (blockY floorDiv CHUNK_SIZE) * CHUNK_SIZE
        val snapshot = snapshot(x, y)
        install(x, y, synchronousMesher.prepare(snapshot))
        val band = effectiveMirrorBand(MIRROR_BAND_BLOCKS, gameWorld.width)
        for (side in mirrorSidesFor(x, gameWorld.width, band)) {
            val offset = side * gameWorld.width
            val body = createBody(x, y, offset, synchronousMesher.prepare(snapshot, offset))
            mirrorBodies.put(Triple(x, y, side), body)?.let(world::destroyBody)
        }
    }

    private fun install(x: Int, y: Int, prepared: CollisionSection) {
        val body = createBody(x, y, 0, prepared)
        // Keep the old body until its replacement is complete; never lose track of a newer body.
        _bodies.put(x to y, body)?.let(world::destroyBody)
    }

    private fun createBody(x: Int, y: Int, xOffset: Int, prepared: CollisionSection): Body {
        check(GdxMainThread.isMainThread()) { "Collision bodies must be installed on the main thread" }
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set((x + xOffset).toFloat() + CHUNK_SIZE / 2f, y.toFloat() + CHUNK_SIZE / 2f)
            fixedRotation = true
        }
        val body = world.createBody(bodyDef)
        try {
            body.userData = prepared.userData
            val fixtureDef = FixtureDef().apply {
                density = 1f
                friction = 0f
                restitution = 0f
            }
            for (fixture in prepared.fixtures) {
                if (fixture.vertices.size < 4) continue
                val shape = ChainShape()
                try {
                    shape.createChain(fixture.vertices)
                    fixtureDef.shape = shape
                    fixtureDef.filter.categoryBits = PhysicsConstants.CATEGORY_BLOCK
                    if (fixture.block.params.castsShadows) {
                        fixtureDef.filter.categoryBits = fixtureDef.filter.categoryBits or PhysicsConstants.CATEGORY_OPAQUE
                    }
                    body.createFixture(fixtureDef).userData = fixture.block
                } finally {
                    shape.dispose()
                }
            }
        } catch (error: Throwable) {
            world.destroyBody(body)
            throw error
        }
        return body
    }

    companion object {
        private const val CHUNK_SIZE = ChunkCollisionMesher.SIZE
        private const val MIRROR_BAND_BLOCKS = 80
    }
}
