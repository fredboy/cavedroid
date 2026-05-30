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
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.world.IWorldRenderer
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min

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

    private val baseSkyColor = Color()
    private val skyLeftColor = Color()
    private val skyRightColor = Color()

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
            x = player.x / 2f + player.aimX / 2f
            y = player.y / 2f + player.aimY / 2f
        }

        val moveVector = cameraTargetPosition.sub(camera.position)

        if (!moveVector.isZero(0.05f)) {
            val l = moveVector.len()
            moveVector.nor().scl(min(l, 30f * delta))
        }

        camera.position.add(moveVector)
    }

    private fun wrapCameraToPlayerSeam() {
        if (gameWorld.isInfinite) {
            // No seam to wrap across in an infinite world.
            return
        }

        val worldWidth = gameWorld.width.toFloat()
        val cameraToPlayer = player.x - camera.position.x
        if (cameraToPlayer > worldWidth / 2f) {
            camera.position.add(Vector3(worldWidth, 0f, 0f))
        } else if (cameraToPlayer < -worldWidth / 2f) {
            camera.position.sub(Vector3(worldWidth, 0f, 0f))
        }
    }

    private fun updateCameraPosition(delta: Float) {
        wrapCameraToPlayerSeam()
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
        gameContextRepository.getCameraContext().visibleWorld.set(getVisibleWorldRect())
        camera.update()
    }

    private fun getVisibleWorldRect(): Rectangle {
        return Rectangle(
            camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth,
            camera.viewportHeight,
        )
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

    private fun updateSkyColors() {
        val sunlight = gameWorld.getSunlight()
        val normalizedTime = gameWorld.getNormalizedTime()

        val base = baseSkyColor.apply {
            if (sunlight < 0.5f) {
                set(MIDNIGHT_COLOR).lerp(HORIZON_COLOR, sunlight * 2f)
            } else {
                set(HORIZON_COLOR).lerp(NOON_COLOR, (sunlight - 0.5f) * 2f)
            }
        }

        val twilight = (1f - 2f * abs(sunlight - 0.5f)).coerceIn(0f, 1f)
        val warmSide = -cos(normalizedTime * PI.toFloat())
        val warmBlend = twilight * abs(warmSide)
        val darkBlend = warmBlend * 0.5f

        if (warmSide < 0f) {
            skyLeftColor.set(base).lerp(HORIZON_COLOR, warmBlend)
            skyRightColor.set(base).lerp(MIDNIGHT_COLOR, darkBlend)
        } else {
            skyLeftColor.set(base).lerp(MIDNIGHT_COLOR, darkBlend)
            skyRightColor.set(base).lerp(HORIZON_COLOR, warmBlend)
        }

        val biomeFactor = gameWorld.biomeProximityFactor(
            centerX = player.x,
            rangeBlocks = WEATHER_SKY_FADE_BLOCKS,
        ) { it != Biome.DESERT }
        val intensity = gameWorld.weatherIntensity * biomeFactor
        if (intensity > 0f) {
            val factor = 1f - intensity * (1f - RAIN_SKY_DARKENING)
            skyLeftColor.r *= factor
            skyLeftColor.g *= factor
            skyLeftColor.b *= factor
            skyRightColor.r *= factor
            skyRightColor.g *= factor
            skyRightColor.b *= factor
        }
    }

    private fun drawSky() {
        val x = camera.position.x - camera.viewportWidth / 2f
        val y = camera.position.y - camera.viewportHeight / 2f
        shaper.begin(ShapeRenderer.ShapeType.Filled)
        shaper.rect(
            /* x = */ x,
            /* y = */ y,
            /* width = */ camera.viewportWidth,
            /* height = */ camera.viewportHeight,
            /* col1 = */ skyLeftColor,
            /* col2 = */ skyRightColor,
            /* col3 = */ skyRightColor,
            /* col4 = */ skyLeftColor,
        )
        shaper.end()
    }

    fun render(delta: Float) {
        updateCameraPosition(delta)

        spriter.projectionMatrix = camera.combined
        shaper.projectionMatrix = camera.combined

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        updateSkyColors()
        drawSky()

        spriter.begin()
        val cameraViewport = Rectangle(
            /* x = */ camera.position.x - camera.viewportWidth / 2,
            /* y = */ camera.position.y - camera.viewportHeight / 2,
            /* width = */ camera.viewportWidth,
            /* height = */ camera.viewportHeight,
        )
        worldRenderers.forEach { renderer -> renderer.draw(spriter, shaper, cameraViewport, delta) }

        spriter.end()

        lightingSystem.render(camera)

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
        private val HORIZON_COLOR = Color(0.9f, 0.45f, 0.25f, 1f)
        private val NOON_COLOR = Color(0.4f, 0.7f, 1f, 1f)

        private const val RAIN_SKY_DARKENING = 0.3f
        private const val WEATHER_SKY_FADE_BLOCKS = 16f

        private const val CAMERA_DELTA = 1f / 60f
    }
}
