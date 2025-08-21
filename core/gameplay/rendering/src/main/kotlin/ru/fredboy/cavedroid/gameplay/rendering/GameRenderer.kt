package ru.fredboy.cavedroid.gameplay.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.window.TooltipManager
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.world.IWorldRenderer
import javax.inject.Inject
import kotlin.math.min

@GameScope
class GameRenderer @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val tooltipManager: TooltipManager,
    private val gameWorld: GameWorld,
    private val player: PlayerAdapter,
    private val getFont: GetFontUseCase,
    @Suppress("LocalVariableName") _worldRenderers: Set<@JvmSuppressWildcards IWorldRenderer>,
    @Suppress("LocalVariableName") _hudRenderers: Set<@JvmSuppressWildcards IHudRenderer>,
) {

    private val worldRenderers = _worldRenderers.sortedBy { it.renderLayer }
    private val hudRenderers = _hudRenderers.sortedBy { it.renderLayer }

    private val debugRenderer = Box2DDebugRenderer(
        /* drawBodies = */ true,
        /* drawJoints = */ false,
        /* drawAABBs = */ false,
        /* drawInactiveBodies = */ false,
        /* drawVelocities = */ false,
        /* drawContacts = */ true,
    )

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

    private var cameraDelayMs: Long = TimeUtils.millis()
    private val cameraCenterToPlayer: Vector2 = Vector2()

    init {
        Gdx.gl.glClearColor(0f, .6f, .6f, 1f)
    }

    private fun updateStaticCameraPosition(targetX: Float, targetY: Float) {
        camera.position.set(targetX, targetY, 0f)
    }

    private fun updateStaticCameraPositionToPlayer() {
        updateStaticCameraPosition(player.x, player.y)
    }

    private fun updateDynamicCameraPosition(delta: Float) {
        val cameraTargetPosition = Vector3()

        val followPlayer = player.controlMode == Player.ControlMode.WALK || !applicationContextRepository.isTouch()

        if (followPlayer) {
            cameraTargetPosition.x = player.x + min(player.controlVector.x * 2, camera.viewportWidth / 2)
            cameraTargetPosition.y = player.y + player.velocity.y
        } else {
            cameraTargetPosition.x = player.cursorX + .5f
            cameraTargetPosition.y = player.cursorY + .5f
        }

        val moveVector = cameraTargetPosition.sub(camera.position)

        if (followPlayer && player.controlVector.isZero) {
            cameraDelayMs = TimeUtils.millis()
        }

        if (TimeUtils.timeSinceMillis(cameraDelayMs) < DYNAMIC_CAMERA_DELAY_MS && !player.controlVector.isZero) {
            updateStaticCameraPosition(
                targetX = player.x - cameraCenterToPlayer.x,
                targetY = camera.position.y + moveVector.y * delta * 2,
            )
            return
        }

        cameraCenterToPlayer.x = player.x - camera.position.x
        cameraCenterToPlayer.y = player.y - camera.position.y

        val worldWidthScreenOffset: Float = gameWorld.width - camera.viewportWidth / 2

        if (moveVector.x >= worldWidthScreenOffset) {
            camera.position.x += gameWorld.width
            moveVector.x -= gameWorld.width
        } else if (moveVector.x <= -worldWidthScreenOffset) {
            camera.position.x -= gameWorld.width
            moveVector.x += gameWorld.width
        }

        camera.position.add(moveVector.scl(delta * 2))

        val maxCamDistanceToPlayer = camera.viewportWidth

        if (camera.position.x + camera.viewportWidth / 2 > player.x + maxCamDistanceToPlayer) {
            camera.position.x = player.x + maxCamDistanceToPlayer - camera.viewportWidth / 2
        }

        if (camera.position.y + camera.viewportHeight / 2 > player.y + maxCamDistanceToPlayer) {
            camera.position.y = player.y + maxCamDistanceToPlayer - camera.viewportHeight / 2
        }

        if (camera.position.x + camera.viewportWidth / 2 < player.x - maxCamDistanceToPlayer) {
            camera.position.x = player.x - maxCamDistanceToPlayer - camera.viewportWidth / 2
        }

        if (camera.position.y + camera.viewportHeight < player.y - maxCamDistanceToPlayer) {
            camera.position.y = player.y - maxCamDistanceToPlayer - camera.viewportHeight / 2
        }
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
        return MIDNIGHT_COLOR.cpy().lerp(NOON_COLOR, gameWorld.getSunlight())
    }

    private fun renderLights() {
        val cameraPosition = camera.position.cpy()

        val wrap = when {
            camera.position.x + camera.viewportWidth / 2 > gameWorld.width.toFloat() -> -1
            camera.position.x - camera.viewportWidth / 2 < 0f -> 1
            else -> 0
        }

        if (wrap != 0) {
            camera.position.x += gameWorld.width * wrap
            camera.update()
            gameWorld.rayHandler.setCombinedMatrix(camera)
            gameWorld.rayHandler.updateAndRender()
            camera.position.set(cameraPosition)
            camera.update()
        }

        gameWorld.rayHandler.setCombinedMatrix(camera)
        gameWorld.rayHandler.updateAndRender()
    }

    fun render(delta: Float) {
        updateCameraPosition(delta)

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

        renderLights()

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

//        if (gameContextRepository.shouldShowInfo()) {
//            debugRenderer.render(gameWorld.world, camera.combined)
//        }
    }

    fun resetCameraToPlayer() {
        updateStaticCameraPositionToPlayer()
        camera.update()
    }

    companion object {
        private const val DYNAMIC_CAMERA_DELAY_MS = 500L

        private val MIDNIGHT_COLOR = Color(0f, 0f, 0.1f, 1f)
        private val NOON_COLOR = Color(0.4f, 0.7f, 1f, 1f)
    }
}
