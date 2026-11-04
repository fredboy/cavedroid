package ru.fredboy.cavedroid.gameplay.controls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.common.utils.TutorialTapTargets
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTouchButtonsUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse.HotbarMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.touch.JoystickInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.touch.TouchCursorInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.touch.TouchWindowInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.mapper.KeyboardInputActionMapper
import ru.fredboy.cavedroid.gameplay.controls.input.mapper.MouseInputActionMapper

class GameInputProcessorTouchTest {
    private val originalGraphics = Gdx.graphics
    private val joystick = Joystick(4f)
    private val application = mockk<ApplicationContextRepository> {
        every { isTouch() } returns true
        every { getWidth() } returns 320f
        every { getHeight() } returns 240f
    }
    private val context = mockk<GameContextRepository>(relaxed = true) {
        every { getJoystick() } returns joystick
        every { getCameraContext().getViewportCoordinates(any(), any()) } answers { firstArg<Int>().toFloat() to secondArg<Int>().toFloat() }
    }
    private val player = mockk<Player>(relaxed = true) {
        every { canJump } returns true
        every { controlVector } returns Vector2()
    }
    private val mobs = mockk<MobController> { every { player } returns this@GameInputProcessorTouchTest.player }
    private val vehicles = mockk<VehicleController>(relaxed = true)
    private val windows = mockk<GameWindowsManager> { every { currentWindowType } returns GameWindowType.NONE }
    private val textures = mockk<GetTextureRegionByNameUseCase>()
    private val buttons = mockk<GetTouchButtonsUseCase>()
    private val cursor = mockk<TouchCursorInputHandler>(relaxed = true)
    private val hotbar = mockk<HotbarMouseInputHandler>(relaxed = true)
    private val touchWindow = mockk<TouchWindowInputHandler>(relaxed = true)
    private val input = GameInputProcessor(
        application, context, buttons, mockk(), MouseInputActionMapper(application), KeyboardInputActionMapper(application),
        setOf(JoystickInputHandler(application, context, mobs, vehicles, windows, textures), cursor, hotbar, touchWindow), emptySet(), windows, mobs, vehicles, TutorialTapTargets(),
    )

    @BeforeEach
    fun setUp() {
        every { textures["hotbar"] } returns mockk<TextureRegion> { every { regionHeight } returns 20 }
        every { buttons() } returns mapOf("jump" to TouchButton(Rectangle(150f, 100f, 48f, 48f), Input.Keys.SPACE, false))
        Gdx.graphics = mockk {
            every { width } returns 320
            every { height } returns 240
        }
        mockkStatic(TimeUtils::class)
        every { TimeUtils.millis() } returns 1000L
    }

    @AfterEach
    fun tearDown() {
        Gdx.graphics = originalGraphics
        unmockkStatic(TimeUtils::class)
    }

    @Test
    fun `cursor receives the first small horizontal movement`() {
        input.touchDown(220, 100, 0, 0)
        val drag = MouseInputAction(221f, 100f, MouseInputActionKey.Dragged(0))
        every { cursor.checkConditions(drag) } returns true

        assertTrue(input.touchDragged(221, 100, 0))

        verify(exactly = 1) { cursor.handleDrag(drag, restartHold = false) }
    }

    @Test
    fun `cursor retains the original horizontal and vertical hold restart thresholds`() {
        input.touchDown(220, 100, 0, 0)
        every { cursor.checkConditions(any()) } returns true

        input.touchDragged(235, 100, 0)
        input.touchDragged(236, 100, 0)
        input.touchDragged(220, 101, 0)

        verify { cursor.handleDrag(MouseInputAction(235f, 100f, MouseInputActionKey.Dragged(0)), restartHold = false) }
        verify { cursor.handleDrag(MouseInputAction(236f, 100f, MouseInputActionKey.Dragged(0)), restartHold = true) }
        verify { cursor.handleDrag(MouseInputAction(220f, 101f, MouseInputActionKey.Dragged(0)), restartHold = true) }
    }

    @Test
    fun `inventory keeps its drag threshold and cannot use the cursor bypass`() {
        every { windows.currentWindowType } returns GameWindowType.CHEST
        input.touchDown(220, 100, 0, 0)

        assertFalse(input.touchDragged(221, 101, 0))
        verify(exactly = 0) { touchWindow.handle(match { it.actionKey is MouseInputActionKey.Dragged }) }

        assertTrue(input.touchDragged(222, 100, 0))
        assertTrue(input.touchDragged(220, 102, 0))

        verify(exactly = 1) { touchWindow.handle(MouseInputAction(222f, 100f, MouseInputActionKey.Dragged(0))) }
        verify(exactly = 1) { touchWindow.handle(MouseInputAction(220f, 102f, MouseInputActionKey.Dragged(0))) }
        verify(exactly = 0) { cursor.checkConditions(any()) }
        verify(exactly = 0) { cursor.handle(any()) }
    }

    @Test
    fun `hotbar hold receives no horizontal drag until its threshold is crossed`() {
        every { hotbar.checkConditions(any()) } returns true
        input.touchDown(220, 230, 0, 0)

        assertFalse(input.touchDragged(235, 230, 0))
        verify(exactly = 0) { hotbar.handle(match { it.actionKey is MouseInputActionKey.Dragged }) }

        assertTrue(input.touchDragged(236, 230, 0))
        verify(exactly = 1) { hotbar.handle(MouseInputAction(236f, 230f, MouseInputActionKey.Dragged(0))) }
    }

    @Test
    fun `small horizontal joystick drag is observed even when it returns before release`() {
        assertTrue(input.touchDown(10, 100, 0, 0))
        assertTrue(input.touchDragged(20, 100, 0))
        assertTrue(input.touchDragged(10, 100, 0))
        assertTrue(input.touchUp(10, 100, 0, 0))
        verify(exactly = 0) { player.jump() }
    }

    @Test
    fun `crossing a touch button keeps the joystick gesture active until release`() {
        input.touchDown(10, 100, 0, 0)
        input.touchDragged(160, 100, 0)
        assertTrue(joystick.active)
        input.touchUp(160, 100, 0, 0)
        assertFalse(joystick.active)
        verify(exactly = 0) { player.jump() }
    }

    @Test
    fun `another finger cannot move or release the active joystick`() {
        input.touchDown(10, 100, 0, 0)
        input.touchDown(220, 100, 1, 0)
        input.touchDragged(240, 100, 1)
        input.touchUp(240, 100, 1, 0)
        assertTrue(joystick.active)
        input.touchUp(10, 100, 0, 0)
        verify(exactly = 1) { player.jump() }
    }
}
