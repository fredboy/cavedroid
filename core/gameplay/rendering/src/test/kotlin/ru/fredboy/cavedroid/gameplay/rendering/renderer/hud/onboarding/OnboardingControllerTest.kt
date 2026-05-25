package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.utils.Vector2Proxy
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager

class OnboardingControllerTest {

    private val applicationContextRepository = mockk<ApplicationContextRepository>(relaxed = true)
    private val mobController = mockk<MobController>(relaxed = true)
    private val gameWindowsManager = mockk<GameWindowsManager>(relaxed = true)
    private val fontAssets = mockk<FontTextureAssetsRepository>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)

    private val controlVector = Vector2(0f, 0f)
    private val velocityVector = Vector2(0f, 0f)
    private val velocityProxy = Vector2Proxy(
        getVelocity = { velocityVector },
        setVelocity = { velocityVector.set(it.x, it.y) },
    )

    private fun setupPlayerState(
        cursorX: Float = 0f,
        cursorY: Float = 0f,
        hittingWithDamage: Boolean = false,
    ) {
        every { mobController.player } returns player
        every { player.controlVector } returns controlVector
        every { player.velocity } returns velocityProxy
        every { player.aimToPlayer } returns Vector2(cursorX, cursorY)
        every { player.isHittingWithDamage } returns hittingWithDamage
    }

    private fun setupContext(isTouch: Boolean, isOnboardingShown: Boolean = false) {
        every { applicationContextRepository.isTouch() } returns isTouch
        every { applicationContextRepository.isOnboardingShown() } returns isOnboardingShown
        every { gameWindowsManager.currentWindowType } returns GameWindowType.NONE
    }

    private fun newController(): OnboardingController = OnboardingController(
        applicationContextRepository = applicationContextRepository,
        mobController = mobController,
        gameWindowsManager = gameWindowsManager,
        fontTextureAssetsRepository = fontAssets,
    )

    private fun OnboardingController.advancePast(step: OnboardingStep) {
        when (step) {
            OnboardingStep.Move -> {
                controlVector.set(1f, 0f)
                update(0.016f)
                update(2f)
                controlVector.set(0f, 0f)
            }

            OnboardingStep.Jump -> {
                velocityVector.y = -5f
                update(0.016f)
                update(2f)
                velocityVector.y = 0f
            }

            OnboardingStep.Aim -> {
                update(0.016f) // baseline
                every { player.aimToPlayer } returns Vector2(5f, 5f)
                update(0.016f)
                update(2f)
            }

            OnboardingStep.Break -> {
                every { player.isHittingWithDamage } returns true
                update(0.016f)
                update(2f)
                every { player.isHittingWithDamage } returns false
            }

            OnboardingStep.Place -> {
                notifyPlace()
                update(0.016f)
                update(2f)
            }

            OnboardingStep.OpenInventory -> {
                every { gameWindowsManager.currentWindowType } returns GameWindowType.SURVIVAL_INVENTORY
                update(0.016f)
                update(2f)
                every { gameWindowsManager.currentWindowType } returns GameWindowType.NONE
            }
        }
    }

    @Test
    fun `state is null when onboarding already shown`() {
        setupContext(isTouch = false, isOnboardingShown = true)
        setupPlayerState()

        val controller = newController()

        assertNull(controller.state)
    }

    @Test
    fun `tick is no-op when onboarding already shown`() {
        setupContext(isTouch = false, isOnboardingShown = true)
        setupPlayerState()

        val controller = newController()
        controller.update(0.016f)

        assertNull(controller.state)
        verify(exactly = 0) { applicationContextRepository.setOnboardingShown(any()) }
    }

    @Test
    fun `kbm flow starts at Move step`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()

        assertSame(OnboardingStep.Move, controller.state?.step)
        assertEquals(true, controller.state?.awaitingInput)
    }

    @Test
    fun `Move advances to Jump on both modes`() {
        setupContext(isTouch = false)
        setupPlayerState()
        val kbm = newController()
        kbm.advancePast(OnboardingStep.Move)
        assertSame(OnboardingStep.Jump, kbm.state?.step)

        setupContext(isTouch = true)
        setupPlayerState()
        val touch = newController()
        touch.advancePast(OnboardingStep.Move)
        assertSame(OnboardingStep.Jump, touch.state?.step)
    }

    @Test
    fun `kbm flow Jump then skips Aim`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        controller.advancePast(OnboardingStep.Jump)

        // Aim is touch-only, so KbM goes straight to Break
        assertSame(OnboardingStep.Break, controller.state?.step)
    }

    @Test
    fun `touch flow includes Aim after Jump`() {
        setupContext(isTouch = true)
        setupPlayerState()

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        controller.advancePast(OnboardingStep.Jump)
        assertSame(OnboardingStep.Aim, controller.state?.step)
    }

    @Test
    fun `touch flow completes without OpenInventory`() {
        setupContext(isTouch = true)
        setupPlayerState()

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        controller.advancePast(OnboardingStep.Jump)
        controller.advancePast(OnboardingStep.Aim)
        controller.advancePast(OnboardingStep.Break)
        controller.advancePast(OnboardingStep.Place)

        assertNull(controller.state)
        verify(exactly = 1) { applicationContextRepository.setOnboardingShown(true) }
    }

    @Test
    fun `kbm flow completes through OpenInventory`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        controller.advancePast(OnboardingStep.Jump)
        controller.advancePast(OnboardingStep.Break)
        controller.advancePast(OnboardingStep.Place)
        controller.advancePast(OnboardingStep.OpenInventory)

        assertNull(controller.state)
        verify(exactly = 1) { applicationContextRepository.setOnboardingShown(true) }
    }

    @Test
    fun `move trigger advances awaitingInput false`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        assertEquals(true, controller.state?.awaitingInput)

        controlVector.set(1f, 0f)
        controller.update(0.016f)

        assertEquals(false, controller.state?.awaitingInput)
        assertSame(OnboardingStep.Move, controller.state?.step)
    }

    @Test
    fun `Jump trigger requires upward velocity past threshold`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        assertSame(OnboardingStep.Jump, controller.state?.step)
        assertEquals(true, controller.state?.awaitingInput)

        // Sub-threshold tiny upward bump should NOT trigger
        velocityVector.y = -1f
        controller.update(0.016f)
        assertEquals(true, controller.state?.awaitingInput)

        // Falling (positive y) should NOT trigger
        velocityVector.y = 5f
        controller.update(0.016f)
        assertEquals(true, controller.state?.awaitingInput)

        // Real jump (past threshold) triggers
        velocityVector.y = -5f
        controller.update(0.016f)
        assertEquals(false, controller.state?.awaitingInput)
    }

    @Test
    fun `cooldown progresses and eventually advances step`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        controlVector.set(1f, 0f)
        controller.update(0.016f) // trigger Move

        controller.update(0.5f)
        assertEquals(OnboardingStep.Move, controller.state?.step)
        assertEquals(false, controller.state?.awaitingInput)
        // 0.5 / 1.5 ≈ 0.33
        val progress = controller.state?.cooldownProgress ?: 0f
        assert(progress > 0.3f && progress < 0.4f) { "progress=$progress" }

        controller.update(2f) // overshoot cooldown
        assertSame(OnboardingStep.Jump, controller.state?.step)
    }

    @Test
    fun `OpenInventory trigger only fires on inventory window types`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        controller.advancePast(OnboardingStep.Jump)
        controller.advancePast(OnboardingStep.Break)
        controller.advancePast(OnboardingStep.Place)

        assertSame(OnboardingStep.OpenInventory, controller.state?.step)

        // Opening a chest should NOT trigger
        every { gameWindowsManager.currentWindowType } returns GameWindowType.CHEST
        controller.update(0.016f)
        assertEquals(true, controller.state?.awaitingInput)

        // Opening crafting table should NOT trigger
        every { gameWindowsManager.currentWindowType } returns GameWindowType.CRAFTING_TABLE
        controller.update(0.016f)
        assertEquals(true, controller.state?.awaitingInput)

        // Opening survival inventory SHOULD trigger
        every { gameWindowsManager.currentWindowType } returns GameWindowType.SURVIVAL_INVENTORY
        controller.update(0.016f)
        assertEquals(false, controller.state?.awaitingInput)
    }

    @Test
    fun `notifyPlace satisfies place trigger`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        controller.advancePast(OnboardingStep.Jump)
        controller.advancePast(OnboardingStep.Break)

        assertSame(OnboardingStep.Place, controller.state?.step)
        assertEquals(true, controller.state?.awaitingInput)

        controller.notifyPlace()
        controller.update(0.016f)

        assertEquals(false, controller.state?.awaitingInput)
    }

    @Test
    fun `Aim trigger requires cursor to differ from baseline`() {
        setupContext(isTouch = true)
        setupPlayerState(cursorX = 10f, cursorY = 10f)

        val controller = newController()
        controller.advancePast(OnboardingStep.Move)
        controller.advancePast(OnboardingStep.Jump)
        assertSame(OnboardingStep.Aim, controller.state?.step)

        // first tick captures baseline; cursor is still at 10,10
        controller.update(0.016f)
        assertEquals(true, controller.state?.awaitingInput)
        // unchanged cursor → still awaiting
        controller.update(0.016f)
        assertEquals(true, controller.state?.awaitingInput)

        // change cursor
        every { player.aimToPlayer } returns Vector2(12f, 12f)
        controller.update(0.016f)
        assertEquals(false, controller.state?.awaitingInput)
    }

    @Test
    fun `tick clamping ignored at controller level - delta is just used as-is`() {
        setupContext(isTouch = false)
        setupPlayerState()

        val controller = newController()
        controlVector.set(1f, 0f)
        controller.update(0.016f) // trigger Move
        controller.update(100f) // huge delta — exhausts cooldown immediately

        assertNotNull(controller.state)
        assert(controller.state?.step != OnboardingStep.Move)
    }
}
