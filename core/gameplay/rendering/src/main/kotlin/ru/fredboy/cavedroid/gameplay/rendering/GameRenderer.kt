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
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.world.IWorldRenderer
import javax.inject.Inject

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
) : Disposable {

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
        val cameraTargetPosition = Vector3().apply {
            x = player.x / 2f + player.cursorX / 2f
            y = player.y / 2f + player.cursorY / 2f
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
        return MIDNIGHT_COLOR.cpy().lerp(NOON_COLOR, gameWorld.getSunlight())
    }

    private fun renderLights() {
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

        if (gameContextRepository.shouldShowInfo()) {
            debugRenderer.render(gameWorld.world, camera.combined)
        }
    }

    fun resetCameraToPlayer() {
        updateStaticCameraPositionToPlayer()
        camera.update()
    }

    override fun dispose() {
        spriter.dispose()
        shaper.dispose()
    }

    companion object {
        private const val DYNAMIC_CAMERA_DELAY_MS = 500L

        private val MIDNIGHT_COLOR = Color(0f, 0f, 0.1f, 1f)
        private val NOON_COLOR = Color(0.4f, 0.7f, 1f, 1f)
    }
}
