package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

class JoystickInputHandlerTest {
    private val movement = Vector2()
    private val player = mockk<Player>(relaxed = true) {
        every { canJump } returns true
        every { gameMode } returns GameMode.SURVIVAL
        every { controlVector } returns movement
    }
    private val mobs = mockk<MobController> { every { player } returns this@JoystickInputHandlerTest.player }
    private val context = mockk<GameContextRepository> { every { getJoystick() } returns Joystick(4f) }
    private val vehicles = mockk<VehicleController>(relaxed = true)
    private val handler = JoystickInputHandler(mockk(), context, mobs, vehicles, mockk(), mockk())
    private var now = 1000L

    @BeforeEach
    fun setUp() {
        mockkStatic(TimeUtils::class)
        every { TimeUtils.millis() } answers { now }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(TimeUtils::class)
    }

    @Test
    fun `quick stationary tap still jumps`() {
        down()
        up()
        verify(exactly = 1) { player.jump() }
    }

    @Test
    fun `tap tolerates small finger drift`() {
        down()
        drag(14f, 12f)
        up(14f, 12f)
        verify(exactly = 1) { player.jump() }
    }

    @Test
    fun `quick drag moves the player without jumping on release`() {
        down()
        drag(58f, 10f)
        assertEquals(4f, movement.x)
        up(58f, 10f)
        assertEquals(0f, movement.x)
        verifyNoTapAction()
    }

    @Test
    fun `drag returning to its starting point is not a tap`() {
        down()
        drag(20f, 10f)
        drag(10f, 10f)
        up()
        verifyNoTapAction()
    }

    @Test
    fun `release displacement rejects a swipe even without drag events`() {
        down()
        up(10f, 30f)
        verifyNoTapAction()
    }

    @Test
    fun `long stationary press does not jump`() {
        down()
        now += 200L
        up()
        verifyNoTapAction()
    }

    @Test
    fun `quick movement while airborne does not enable flight`() {
        every { player.canJump } returns false
        every { player.gameMode } returns GameMode.CREATIVE
        down()
        drag(58f, 10f)
        up(58f, 10f)
        verifyNoTapAction()
    }

    @Test
    fun `quick rowing gesture does not dismount`() {
        every { player.isMounted } returns true
        down()
        drag(58f, 10f)
        up(58f, 10f)
        verifyNoTapAction()
    }

    @Test
    fun `deliberate tap still dismounts`() {
        every { player.isMounted } returns true
        down()
        up()
        verify(exactly = 1) { vehicles.dismount() }
        verify(exactly = 0) { player.jump() }
    }

    @Test
    fun `deliberate airborne creative tap still enables flight`() {
        every { player.canJump } returns false
        every { player.gameMode } returns GameMode.CREATIVE
        down()
        up()
        verify(exactly = 1) { player.isFlyMode = true }
    }

    @Test
    fun `new tap after a drag can jump`() {
        down()
        drag(58f, 10f)
        up(58f, 10f)
        down()
        up()
        verify(exactly = 1) { player.jump() }
    }

    @Test
    fun `resetting active touch input does not request dismount or flight`() {
        every { player.isMounted } returns true
        down()
        handler.reset()
        up()
        verifyNoTapAction()
    }

    private fun down() {
        handler.handle(MouseInputAction(10f, 10f, MouseInputActionKey.Screen(false, 0)))
    }

    private fun drag(x: Float, y: Float) {
        handler.handle(MouseInputAction(x, y, MouseInputActionKey.Dragged(0)))
    }

    private fun up(x: Float = 10f, y: Float = 10f) {
        now += 50L
        handler.handle(MouseInputAction(x, y, MouseInputActionKey.Screen(true, 0)))
    }

    private fun verifyNoTapAction() {
        verify(exactly = 0) { vehicles.dismount() }
        verify(exactly = 0) { player.jump() }
        verify(exactly = 0) { player.isFlyMode = true }
    }
}
