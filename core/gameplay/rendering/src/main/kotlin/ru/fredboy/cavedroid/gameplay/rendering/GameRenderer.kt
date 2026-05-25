package ru.fredboy.cavedroid.gameplay.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.common.utils.floorDiv
import ru.fredboy.cavedroid.common.utils.floorMod
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.world.IWorldRenderer
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

@GameScope
class GameRenderer @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val tooltipManager: TooltipManager,
    private val gameWorld: GameWorld,
    private val lightingSystem: LightingSystem,
    private val player: PlayerAdapter,
    private val getFont: GetFontUseCase,
    @Suppress("LocalVariableName") _worldRenderers: Set<@JvmSuppressWildcards IWorldRenderer>,
    @Suppress("LocalVariableName") _hudRenderers: Set<@JvmSuppressWildcards IHudRenderer>,
) : Disposable {

    private val worldRenderers = _worldRenderers.sortedBy { it.renderLayer }
    private val hudRenderers = _hudRenderers.sortedBy { it.renderLayer }

    private val debugRenderer = ifTrue(applicationContextRepository.isDebug()) {
        Box2DDebugRenderer(
            /* drawBodies = */ true,
            /* drawJoints = */ false,
            /* drawAABBs = */ false,
            /* drawInactiveBodies = */ false,
            /* drawVelocities = */ false,
            /* drawContacts = */ true,
        )
    }

    private val camera = OrthographicCamera()
        .apply {
            setToOrtho(
                /* yDown = */ true,
                /* viewportWidth = */ applicationContextRepository.getWidth().meters,
                /* viewportHeight = */ applicationContextRepository.getHeight().meters,
            )
        }

    private val hudCamera = OrthographicCamera()
        .apply {
            setToOrtho(
                /* yDown = */ true,
                /* viewportWidth = */ applicationContextRepository.getWidth(),
                /* viewportHeight = */ applicationContextRepository.getHeight(),
            )
        }

    private val shaper = ShapeRenderer()

    private val spriter = SpriteBatch()

    private var lastCameraX = Float.NaN

    private val activeLightChunks = mutableSetOf<Pair<Int, Int>>()
    private val scratchVisibleChunks = mutableSetOf<Pair<Int, Int>>()
    private val scratchEnteredChunks = mutableSetOf<Pair<Int, Int>>()

    init {
        Gdx.gl.glClearColor(0f, .6f, .6f, 1f)
    }

    private fun refreshVisibleLightChunks() {
        val chunkSize = lightingSystem.chunkSize
        if (chunkSize <= 0) return

        val worldWidth = gameWorld.width
        val worldHeight = gameWorld.height
        if (worldWidth <= 0 || worldHeight <= 0) return

        val visibleX = camera.position.x - camera.viewportWidth / 2f
        val visibleY = camera.position.y - camera.viewportHeight / 2f
        val visibleRight = visibleX + camera.viewportWidth
        val visibleBottom = visibleY + camera.viewportHeight

        val minBlockX = floor(visibleX).toInt() - chunkSize
        val maxBlockX = ceil(visibleRight).toInt() + chunkSize
        val minBlockY = floor(visibleY).toInt() - chunkSize
        val maxBlockY = ceil(visibleBottom).toInt() + chunkSize

        val chunkMinX = (minBlockX floorDiv chunkSize) * chunkSize
        val chunkMaxX = (maxBlockX floorDiv chunkSize) * chunkSize
        val chunkMinY = (minBlockY floorDiv chunkSize) * chunkSize
        val chunkMaxY = (maxBlockY floorDiv chunkSize) * chunkSize

        scratchVisibleChunks.clear()
        var cx = chunkMinX
        while (cx <= chunkMaxX) {
            val wrappedX = cx floorMod worldWidth
            val wrappedChunkX = wrappedX - (wrappedX floorMod chunkSize)
            var cy = chunkMinY
            while (cy <= chunkMaxY) {
                if (cy in 0 until worldHeight) {
                    scratchVisibleChunks.add(wrappedChunkX to cy)
                }
                cy += chunkSize
            }
            cx += chunkSize
        }

        scratchEnteredChunks.clear()
        scratchEnteredChunks.addAll(scratchVisibleChunks)
        scratchEnteredChunks.removeAll(activeLightChunks)

        if (scratchEnteredChunks.isNotEmpty()) {
            lightingSystem.refreshChunks(scratchEnteredChunks)
        }

        activeLightChunks.clear()
        activeLightChunks.addAll(scratchVisibleChunks)
    }

    private fun updateStaticCameraPosition(targetX: Float, targetY: Float) {
        camera.position.set(targetX, targetY, 0f)
    }

    private fun updateStaticCameraPositionToPlayer() {
        updateStaticCameraPosition(player.x, player.y)
    }

    private fun updateDynamicCameraPosition(delta: Float) {
        val cameraTargetPosition = Vector3().apply {
            x = player.x / 2f + player.aimX / 2f
            y = player.y / 2f + player.aimY / 2f
        }

        if (!gameContextRepository.getCameraContext().visibleWorld.contains(player.x, player.y)) {
            camera.position.set(cameraTargetPosition)
            return
        }

        val moveVector = cameraTargetPosition.sub(camera.position)

        if (!moveVector.isZero(0.05f)) {
            moveVector.nor().scl(30f * delta)
        }

        camera.position.add(moveVector)
    }

    private fun updateCameraPosition(delta: Float) {
        if (applicationContextRepository.useDynamicCamera()) {
            updateDynamicCameraPosition(delta)
        } else {
            updateStaticCameraPositionToPlayer()
        }
        gameContextRepository.getCameraContext().viewport.apply {
            x = hudCamera.position.x - hudCamera.viewportWidth / 2
            y = hudCamera.position.y - hudCamera.viewportHeight / 2
            width = hudCamera.viewportWidth
            height = hudCamera.viewportHeight
        }
        gameContextRepository.getCameraContext().visibleWorld.apply {
            x = camera.position.x - camera.viewportWidth / 2
            y = camera.position.y - camera.viewportHeight / 2
            width = camera.viewportWidth
            height = camera.viewportHeight
        }
        camera.update()
    }

    private fun handleMousePosition() {
        val viewportX = hudCamera.viewportWidth / Gdx.graphics.width * Gdx.input.x
        val viewportY = hudCamera.viewportHeight / Gdx.graphics.height * Gdx.input.y

        if (tooltipManager.currentMouseTooltip.isNotEmpty()) {
            spriter.drawString(
                font = getFont(),
                str = tooltipManager.currentMouseTooltip,
                x = viewportX + 1f,
                y = viewportY + 1f,
                color = Color.BLACK,
            )
            spriter.drawString(
                font = getFont(),
                str = tooltipManager.currentMouseTooltip,
                x = viewportX,
                y = viewportY,
                color = Color.WHITE,
            )
        }
    }

    fun onResize() {
        camera.setToOrtho(
            /* yDown = */ true,
            /* viewportWidth = */ applicationContextRepository.getWidth().meters,
            /* viewportHeight = */ applicationContextRepository.getHeight().meters,
        )
        hudCamera.setToOrtho(
            /* yDown = */ true,
            /* viewportWidth = */ applicationContextRepository.getWidth(),
            /* viewportHeight = */ applicationContextRepository.getHeight(),
        )
        resetCameraToPlayer()
    }

    private fun getSkyColor(): Color {
        val color = MIDNIGHT_COLOR.cpy().lerp(NOON_COLOR, gameWorld.getSunlight())
        val intensity = gameWorld.weatherIntensity
        if (intensity > 0f) {
            val factor = 1f - intensity * (1f - RAIN_SKY_DARKENING)
            color.r *= factor
            color.g *= factor
            color.b *= factor
        }
        return color
    }

    fun render(delta: Float) {
        updateCameraPosition(delta)

        val cameraJumped = !lastCameraX.isNaN() &&
            abs(camera.position.x - lastCameraX) > gameWorld.width / 2f
        lastCameraX = camera.position.x

        if (cameraJumped) {
            activeLightChunks.clear()
        }
        refreshVisibleLightChunks()

        spriter.projectionMatrix = camera.combined
        shaper.projectionMatrix = camera.combined

        val bgColor = getSkyColor()
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        spriter.begin()
        val cameraViewport = Rectangle(
            /* x = */ camera.position.x - camera.viewportWidth / 2,
            /* y = */ camera.position.y - camera.viewportHeight / 2,
            /* width = */ camera.viewportWidth,
            /* height = */ camera.viewportHeight,
        )
        worldRenderers.forEach { renderer -> renderer.draw(spriter, shaper, cameraViewport, delta) }

        spriter.end()

        lightingSystem.render(camera, cameraJumped)

        if (gameContextRepository.shouldShowInfo()) {
            debugRenderer?.render(gameWorld.world, camera.combined)
        }

        spriter.projectionMatrix = hudCamera.combined
        shaper.projectionMatrix = hudCamera.combined

        spriter.begin()
        val hudCameraViewport = Rectangle(
            /* x = */ hudCamera.position.x - hudCamera.viewportWidth / 2,
            /* y = */ hudCamera.position.y - hudCamera.viewportHeight / 2,
            /* width = */ hudCamera.viewportWidth,
            /* height = */ hudCamera.viewportHeight,
        )
        hudRenderers.forEach { renderer -> renderer.draw(spriter, shaper, hudCameraViewport, delta) }
        handleMousePosition()
        spriter.end()
    }

    fun resetCameraToPlayer() {
        updateStaticCameraPositionToPlayer()
        camera.update()
    }

    override fun dispose() {
        spriter.dispose()
        shaper.dispose()
        debugRenderer?.dispose()
        worldRenderers.forEach { renderer -> renderer.dispose() }
        hudRenderers.forEach { renderer -> renderer.dispose() }
    }

    companion object {
        private val MIDNIGHT_COLOR = Color(0f, 0f, 0.1f, 1f)
        private val NOON_COLOR = Color(0.4f, 0.7f, 1f, 1f)

        private const val RAIN_SKY_DARKENING = 0.3f
    }
}
