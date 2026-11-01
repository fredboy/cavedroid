package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

class TouchCursorInputHandlerTest {
    private val originalApp = Gdx.app
    private val player = mockk<Player>(relaxed = true) {
        every { position } returns Vector2()
        every { aimToPlayer } returns Vector2()
    }
    private val mobs = mockk<MobController>(relaxed = true) { every { player } returns this@TouchCursorInputHandlerTest.player }
    private val vehicles = mockk<VehicleController>(relaxed = true)
    private val projectiles = mockk<ProjectileController>(relaxed = true)
    private val items = mockk<GetItemByKeyUseCase>(relaxed = true)
    private val scheduled = mutableListOf<Timer.Task>()
    private val handler = TouchCursorInputHandler(
        mockk(), mockk(), mobs, vehicles, mockk(), mockk(), emptyMap(), emptyMap(), emptyMap(), emptyMap(),
        mockk(), mockk(), projectiles, items, mockk(), mockk(), mockk(),
    )

    @BeforeEach
    fun setUp() {
        Gdx.app = mockk(relaxed = true)
        mockkStatic(Timer::class)
        every { Timer.schedule(any<Timer.Task>(), any<Float>()) } answers {
            firstArg<Timer.Task>().also { scheduled.add(it) }
        }
    }

    @AfterEach
    fun tearDown() {
        handler.reset()
        unmockkStatic(Timer::class)
        Gdx.app = originalApp
    }

    @Test
    fun `small movements update aim without postponing hold or resetting damage`() {
        handler.handle(MouseInputAction(220f, 100f, MouseInputActionKey.Screen(false, 0)))
        handler.handleDrag(MouseInputAction(221f, 100f, MouseInputActionKey.Dragged(0)), restartHold = false)
        handler.handleDrag(MouseInputAction(222f, 100f, MouseInputActionKey.Dragged(0)), restartHold = false)

        verify { player.aimX = 1f.meters }
        verify { player.aimX = 2f.meters }
        verify(exactly = 1) { Timer.schedule(any<Timer.Task>(), any<Float>()) }
        verify(exactly = 0) { player.blockDamage = any() }
    }

    @Test
    fun `movement above the threshold restarts hold even within the same selected block`() {
        handler.handle(MouseInputAction(220f, 100f, MouseInputActionKey.Screen(false, 0)))
        handler.handle(MouseInputAction(236f, 100f, MouseInputActionKey.Dragged(0)))

        verify(exactly = 2) { Timer.schedule(any<Timer.Task>(), any<Float>()) }
        verify(exactly = 0) { player.blockDamage = any() }
    }

    @Test
    fun `small movement preserves boat boarding hold and does not repeat it`() {
        every { vehicles.boardTarget() } returns true
        handler.handle(MouseInputAction(220f, 100f, MouseInputActionKey.Screen(false, 0)))
        handler.handleDrag(MouseInputAction(221f, 100f, MouseInputActionKey.Dragged(0)), restartHold = false)
        scheduled.single().run()

        handler.handleDrag(MouseInputAction(221f, 100f, MouseInputActionKey.Dragged(0)), restartHold = false)

        verify(exactly = 1) { vehicles.boardTarget() }
        verify(exactly = 0) { player.canShootBow() }
        verify(exactly = 1) { Timer.schedule(any<Timer.Task>(), any<Float>()) }
    }

    @Test
    fun `tiny drag across a block boundary preserves the hold timer`() {
        handler.handle(MouseInputAction(220f, 100f, MouseInputActionKey.Screen(false, 0)))
        every { mobs.rayCastPlayerCursor(any()) } answers {
            every { player.selectedX } returns 1
        }

        handler.handleDrag(MouseInputAction(221f, 100f, MouseInputActionKey.Dragged(0)), restartHold = false)

        verify(exactly = 1) { Timer.schedule(any<Timer.Task>(), any<Float>()) }
        verify(exactly = 1) { player.blockDamage = 0f }
    }

    @Test
    fun `bow charges despite tiny drags and fires once on release after aiming`() {
        var pulling = false
        every { player.isPullingBow } answers { pulling }
        every { player.isPullingBow = any() } answers { pulling = firstArg() }
        every { player.canShootBow() } returns true
        every { player.direction } returns Direction.RIGHT
        handler.handle(MouseInputAction(220f, 100f, MouseInputActionKey.Screen(false, 0)))
        handler.handleDrag(MouseInputAction(221f, 100f, MouseInputActionKey.Dragged(0)), restartHold = false)
        verify(exactly = 1) { Timer.schedule(any<Timer.Task>(), any<Float>()) }
        scheduled.single().run()
        verify(exactly = 1) { player.isPullingBow = true }

        handler.handleDrag(MouseInputAction(240f, 100f, MouseInputActionKey.Dragged(0)), restartHold = true)
        verify(exactly = 0) { player.isPullingBow = false }
        handler.handle(MouseInputAction(240f, 100f, MouseInputActionKey.Screen(true, 0)))

        verify(exactly = 1) { projectiles.addProjectile(any(), any(), any(), any()) }
        verify(exactly = 1) { player.decreaseArrows() }
        verify(exactly = 1) { player.durateActiveDurable() }
        verify(exactly = 1) { player.isPullingBow = false }
    }
}
