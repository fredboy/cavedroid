package ru.fredboy.cavedroid.gameplay.lighting.bfs

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.Body
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

class BfsLightingSystem(
    private val gameContextRepository: GameContextRepository,
) : LightingSystem,
    OnBlockPlacedListener {

    private var _gameWorld: GameWorld? = null
    private var _grid: LightGrid? = null
    private var _overlay: LightingOverlayRenderer? = null

    private val transientEmitterIds = AtomicLong(0L)

    private val gameWorld: GameWorld
        get() = requireNotNull(_gameWorld)

    override val chunkSize: Int = 1

    override fun attachToGameWorld(gameWorld: GameWorld) {
        if (_gameWorld != null) {
            logger.w { "BfsLightingSystem already attached" }
            return
        }

        _gameWorld = gameWorld

        if (gameWorld.isInfinite) {
            // TODO(M5): replace with a windowed light grid that follows the player. For now infinite
            // worlds run unlit (full daylight brightness) rather than allocating a grid for the
            // whole (unbounded) world.
            logger.w { "BfsLightingSystem running in degraded mode for infinite world" }
            return
        }

        _grid = LightGrid(gameWorld.width, gameWorld.height).also { lightGrid ->
            lightGrid.rebuildAll(
                isOpaque = { x, y -> isOpaque(gameWorld, x, y) },
                blockEmission = { x, y -> emissionAt(gameWorld, x, y) },
            )
        }
        _overlay = LightingOverlayRenderer(gameContextRepository, gameWorld, requireNotNull(_grid))

        gameWorld.addBlockPlacedListener(this)
    }

    override fun onBlockPlaced(block: Block, x: Int, y: Int, layer: Layer) {
        val world = _gameWorld ?: return
        val grid = _grid ?: return
        grid.onCellChanged(
            x = x,
            y = y,
            newOpaque = isOpaque(world, x, y),
            newEmission = emissionAt(world, x, y),
        )
    }

    override fun update(delta: Float) = Unit

    override fun recalculate() = Unit

    override fun refreshChunks(chunks: Iterable<Pair<Int, Int>>) = Unit

    override fun render(camera: OrthographicCamera) {
        val overlay = _overlay ?: return
        overlay.render(camera, gameWorld.getSunlight())
    }

    override fun isMobExposedToSun(mob: Mob): Boolean {
        val world = _gameWorld ?: return false
        val grid = _grid ?: return false
        return grid.isSkyExposed(mob.mapX, mob.upperMapY.coerceIn(0, world.height - 1))
    }

    override fun createPlayerSightLight(body: Body, x: Float, y: Float): LightHandle = NoOpLightHandle

    override fun createFurnaceLight(x: Float, y: Float): LightHandle {
        if (_gameWorld == null) return NoOpLightHandle
        val id = transientEmitterIds.incrementAndGet()
        return FurnaceLightHandle(id, x, y)
    }

    override fun createFireLight(x: Float, y: Float): LightHandle {
        if (_gameWorld == null) return NoOpLightHandle
        val id = transientEmitterIds.incrementAndGet()
        return FireLightHandle(id, x, y)
    }

    override fun getEffectiveBrightness(x: Int, y: Int, sunBrightness: Float): Float {
        val grid = _grid ?: return sunBrightness
        return grid.effective(x, y, sunBrightness)
    }

    override fun dispose() {
        _gameWorld?.removeBlockPlacedListener(this)
        _overlay?.dispose()
        _overlay = null
        _grid = null
        _gameWorld = null
    }

    private fun isOpaque(world: GameWorld, x: Int, y: Int): Boolean {
        val foreground = world.getForeMap(x, y)
        return foreground.params.castsShadows && foreground.params.hasCollision
    }

    private fun emissionAt(world: GameWorld, x: Int, y: Int): Int {
        return world.getForeMap(x, y).params.lightInfo?.toLevel()
            ?: world.getBackMap(x, y).params.lightInfo?.toLevel()
            ?: 0
    }

    private inner class FurnaceLightHandle(
        private val id: Long,
        startX: Float,
        startY: Float,
    ) : LightHandle {

        private var posX: Float = startX
        private var posY: Float = startY
        private var active: Boolean = true

        init {
            applyToGrid()
        }

        override var isActive: Boolean
            get() = active
            set(value) {
                if (value == active) return
                active = value
                applyToGrid()
            }

        override fun setPosition(x: Float, y: Float) {
            posX = x
            posY = y
            applyToGrid()
        }

        override fun update() {
            applyToGrid()
        }

        override fun dispose() {
            active = false
            applyToGrid()
        }

        private fun applyToGrid() {
            val currentGrid = _grid ?: return
            if (active) {
                currentGrid.setTransientEmitter(id, posX.toInt(), posY.toInt(), FURNACE_LEVEL)
            } else {
                currentGrid.clearTransientEmitter(id)
            }
        }
    }

    private inner class FireLightHandle(
        private val id: Long,
        startX: Float,
        startY: Float,
    ) : LightHandle {

        private var posX: Float = startX
        private var posY: Float = startY
        private var active: Boolean = true

        init {
            applyToGrid()
        }

        override var isActive: Boolean
            get() = active
            set(value) {
                if (value == active) return
                active = value
                applyToGrid()
            }

        override fun setPosition(x: Float, y: Float) {
            posX = x
            posY = y
            applyToGrid()
        }

        override fun update() {
            applyToGrid()
        }

        override fun dispose() {
            active = false
            applyToGrid()
        }

        private fun applyToGrid() {
            val currentGrid = _grid ?: return
            if (active) {
                currentGrid.setTransientEmitter(id, posX.toInt(), posY.toInt(), FIRE_LEVEL)
            } else {
                currentGrid.clearTransientEmitter(id)
            }
        }
    }

    private object NoOpLightHandle : LightHandle {
        override var isActive: Boolean = true
        override fun setPosition(x: Float, y: Float) = Unit
        override fun update() = Unit
        override fun dispose() = Unit
    }

    companion object {
        private const val TAG = "BfsLightingSystem"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val FURNACE_LEVEL = 13
        private const val FIRE_LEVEL = 11

        private fun ru.fredboy.cavedroid.domain.items.model.block.BlockLightInfo.toLevel(): Int {
            val raw = (lightBrightness * LightGrid.MAX_LEVEL_F).roundToInt()
            return raw.coerceIn(0, LightGrid.MAX_LEVEL)
        }
    }
}
