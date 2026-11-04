package ru.fredboy.cavedroid.gameplay.controls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.common.utils.TutorialTapTargets
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard.CloseGameWindowKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard.GoRightKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard.OpenInventoryKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.mapper.KeyboardInputActionMapper

class GameInputProcessorTest {
    private val originalInput = Gdx.input
    private val application = mockk<ApplicationContextRepository>(relaxed = true)
    private val context = mockk<GameContextRepository>(relaxed = true) {
        every { getJoystick() } returns Joystick(4f)
    }
    private val movement = Vector2()
    private val player = mockk<Player>(relaxed = true) {
        every { controlVector } returns movement
        every { speed } returns 4f
        every { gameMode } returns GameMode.SURVIVAL
    }
    private val mobs = mockk<MobController> { every { player } returns this@GameInputProcessorTest.player }
    private val vehicles = mockk<VehicleController>(relaxed = true)
    private var windowType = GameWindowType.NONE
    private val windows = mockk<GameWindowsManager>(relaxed = true) {
        every { currentWindowType } answers { windowType }
        every { currentWindow } returns null
        every { openSurvivalInventory() } answers { windowType = GameWindowType.SURVIVAL_INVENTORY }
        every { openCreativeTabsInventory() } answers { windowType = GameWindowType.CREATIVE_INVENTORY_TABS }
        every { closeWindow() } answers { windowType = GameWindowType.NONE }
    }
    private val tutorialTargets = TutorialTapTargets()
    private val input = GameInputProcessor(
        application, context, mockk(), mockk(relaxed = true), mockk(), KeyboardInputActionMapper(application),
        emptySet(),
        setOf(
            OpenInventoryKeyboardInputHandler(mobs, windows),
            CloseGameWindowKeyboardInputHandler(windows, mockk(), mockk()),
            GoRightKeyboardInputHandler(mobs),
        ),
        windows, mobs, vehicles, tutorialTargets,
    )

    @BeforeEach
    fun setUp() {
        Gdx.input = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Gdx.input = originalInput
    }

    @ParameterizedTest
    @EnumSource(GameMode::class)
    fun `inventory stays open across updates and closes on the next inventory key release`(mode: GameMode) {
        every { player.gameMode } returns mode
        input.keyDown(Input.Keys.D)
        assertTrue(movement.x > 0f)
        input.keyDown(Input.Keys.E)
        input.keyUp(Input.Keys.E)

        repeat(3) { input.update(1f / 60f) }

        val expected = if (mode == GameMode.CREATIVE) GameWindowType.CREATIVE_INVENTORY_TABS else GameWindowType.SURVIVAL_INVENTORY
        assertEquals(expected, windowType)
        assertTrue(movement.isZero)
        verify(exactly = 1) { vehicles.resetMovement() }
        verify(exactly = 0) { windows.closeWindow() }

        input.keyDown(Input.Keys.E)
        input.keyUp(Input.Keys.E)
        input.update(1f / 60f)
        assertEquals(GameWindowType.NONE, windowType)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `toast consumes press drag and release while keeping inventory open`(touch: Boolean) {
        every { application.isTouch() } returns touch
        windowType = GameWindowType.SURVIVAL_INVENTORY
        every { context.getCameraContext().getViewportCoordinates(any(), any()) } answers {
            firstArg<Int>().toFloat() to secondArg<Int>().toFloat()
        }
        var dismissals = 0
        tutorialTargets.register("test", Rectangle(10f, 10f, 100f, 40f)) {
            dismissals++
            true
        }
        assertTrue(input.touchDown(20, 20, 0, Input.Buttons.LEFT))
        input.update(0.016f) // Window transitions must retain gesture ownership.
        assertTrue(input.touchDragged(400, 400, 0))
        assertTrue(input.touchUp(400, 400, 0, Input.Buttons.LEFT))
        assertEquals(1, dismissals)
        assertEquals(GameWindowType.SURVIVAL_INVENTORY, windowType)
        verify(exactly = 0) { windows.closeWindow() }
    }

    @Test
    fun `lifecycle reset still closes an open inventory`() {
        input.keyUp(Input.Keys.E)
        input.update(1f / 60f)

        input.reset()

        assertEquals(GameWindowType.NONE, windowType)
        verify(exactly = 1) { windows.closeWindow() }
    }
}
