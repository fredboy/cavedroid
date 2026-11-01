package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.utils.pixels
import ru.fredboy.cavedroid.domain.assets.repository.MobTextureAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.presentation.PoseHistory
import ru.fredboy.cavedroid.gameplay.rendering.utils.PresentationPoses

class CrosshairRendererTest {

    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController>()
    private val world = mockk<GameWorld>()
    private val vehicles = mockk<VehicleController> { every { mounted } returns null }
    private val poses = PresentationPoses(world, mobs, vehicles, mockk<GameWindowsManager>())
    private val sprite = mockk<Sprite>(relaxed = true)
    private val assets = mockk<MobTextureAssetsRepository> { every { getCrosshairSprite() } returns sprite }
    private val app = mockk<ApplicationContextRepository> { every { isTouch() } returns true }
    private val viewport = Rectangle(0f, 0f, 320f, 192f)
    private val camera = CameraContext(viewport, Rectangle(0f, 0f, 20f, 12f))
    private val context = mockk<GameContextRepository> {
        every { getCameraContext() } returns camera
        every { shouldShowInfo() } returns false
    }
    private val renderer = CrosshairRenderer(mobs, assets, app, context, poses)
    private val batch = mockk<SpriteBatch>()
    private val shapes = mockk<ShapeRenderer>()
    private val physical = Vector2(10.1f, 20.1f)
    private val cursorOffset = Vector2(2f, -1f)
    private val history = PoseHistory()
    private val drawn = mutableListOf<Vector2>()
    private var alpha = 0f

    init {
        every { mobs.player } returns player
        every { player.holdAim } returns true
        every { player.position } returns physical
        every { player.cursorX } answers { physical.x + cursorOffset.x }
        every { player.cursorY } answers { physical.y + cursorOffset.y }
        every { player.aimX } answers { physical.x + 4f }
        every { player.aimY } answers { physical.y - 2f }
        every { world.sampleRenderPosition(player.body, any(), any()) } answers {
            val fraction = if (thirdArg<Boolean>()) 1f else alpha
            secondArg<Vector2>().set(history.sampleX(fraction, 0f), history.sampleY(fraction))
        }
        every { sprite.color } returns Color.WHITE
        every { sprite.regionWidth } returns 8
        every { sprite.regionHeight } returns 8
        every { sprite.setPosition(any(), any()) } answers {
            drawn.add(Vector2(firstArg<Float>() + 4f, secondArg<Float>() + 4f))
        }
        history.beforeStep(10f, 20f)
        history.afterStep(physical.x, physical.y)
    }

    private fun draw() {
        renderer.draw(batch, shapes, viewport, 1f / 120f)
    }

    private fun assertPosition(expected: Vector2, actual: Vector2) {
        assertEquals(expected.x, actual.x, 0.001f)
        assertEquals(expected.y, actual.y, 0.001f)
    }

    @Test
    fun `held crosshair stays steady between physics steps with dynamic camera`() {
        for (frame in 0..2) {
            if (frame == 2) {
                history.beforeStep(physical.x, physical.y)
                physical.add(0.1f, 0.1f)
                history.afterStep(physical.x, physical.y)
            }
            alpha = if (frame == 1) 0.5f else 0f
            val renderPosition = poses.playerPosition(Vector2())
            // The dynamic camera follows the midpoint of rendered player and held aim.
            camera.visibleWorld.setPosition(renderPosition.x + 2f - 10f, renderPosition.y - 1f - 6f)
            draw()
        }

        drawn.forEach { assertPosition(Vector2(160f, 96f), it) }
        verify(exactly = 0) { player.cursorX = any() }
        verify(exactly = 0) { player.cursorY = any() }
    }

    @Test
    fun `ray cast cursor offset is preserved and debug aim uses the same presentation offset`() {
        every { context.shouldShowInfo() } returns true
        alpha = 0.5f
        draw()

        val renderPosition = poses.playerPosition(Vector2())
        assertPosition(renderPosition.cpy().add(cursorOffset).scl(16f), drawn[0])
        assertPosition(renderPosition.cpy().add(4f, -2f).scl(16f), drawn[1])
    }

    @Test
    fun `mouse cursor remains at its gameplay world position`() {
        every { app.isTouch() } returns false
        every { context.shouldShowInfo() } returns true
        every { player.holdAim } returns false
        alpha = 0.5f
        draw()

        assertPosition(Vector2(player.cursorX.pixels, player.cursorY.pixels), drawn[0])
        assertPosition(Vector2(player.aimX.pixels, player.aimY.pixels), drawn[1])
    }

    @Test
    fun `snapshot uses the current physics cursor without interpolation`() {
        poses.snapshot = true
        draw()

        assertPosition(Vector2(player.cursorX.pixels, player.cursorY.pixels), drawn.single())
    }
}
