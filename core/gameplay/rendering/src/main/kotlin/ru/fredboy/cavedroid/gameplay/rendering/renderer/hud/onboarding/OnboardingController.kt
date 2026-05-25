package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.api.OnboardingEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import javax.inject.Inject

@GameScope
class OnboardingController @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val fontTextureAssetsRepository: FontTextureAssetsRepository,
) : OnboardingEvents {

    private val isTouch: Boolean = applicationContextRepository.isTouch()

    private val steps: List<OnboardingStep> = OnboardingStep.ALL.filter { it.isAvailable(isTouch) }

    private var stepIndex = 0

    private var cursorBaseline: Vector2? = null

    private var placeTriggered = false

    var state: OnboardingState? = if (applicationContextRepository.isOnboardingShown() || steps.isEmpty()) {
        null
    } else {
        OnboardingState(
            step = steps[0],
            awaitingInput = true,
            cooldownProgress = 0f,
            isLast = steps.size == 1,
        )
    }
        private set

    fun update(delta: Float) {
        val current = state ?: return
        if (current.awaitingInput) {
            handleAwaitingInput(current)
        } else {
            handleCooldown(current, delta)
        }
    }

    fun getLocalizedTip(step: OnboardingStep): String = fontTextureAssetsRepository
        .getOnboardingLocalizationBundle()
        .get(step.localizationKey(isTouch))

    override fun notifyPlace() {
        if (stepIndex < steps.size && steps[stepIndex] != OnboardingStep.Place) {
            return
        }

        placeTriggered = true
    }

    private fun handleAwaitingInput(current: OnboardingState) {
        if (current.step === OnboardingStep.Aim && cursorBaseline == null) {
            val player = mobController.player
            cursorBaseline = player.aimToPlayer.cpy()
            return
        }
        if (isTriggered(current.step)) {
            state = current.copy(awaitingInput = false, cooldownProgress = 0f)
        }
    }

    private fun handleCooldown(current: OnboardingState, delta: Float) {
        val newProgress = (current.cooldownProgress + delta / COOLDOWN_SEC).coerceAtMost(1f)
        if (newProgress >= 1f) {
            advance()
        } else {
            state = current.copy(cooldownProgress = newProgress)
        }
    }

    private fun advance() {
        stepIndex++
        cursorBaseline = null
        placeTriggered = false
        state = if (stepIndex >= steps.size) {
            applicationContextRepository.setOnboardingShown(true)
            null
        } else {
            OnboardingState(
                step = steps[stepIndex],
                awaitingInput = true,
                cooldownProgress = 0f,
                isLast = stepIndex == steps.size - 1,
            )
        }
    }

    private fun isTriggered(step: OnboardingStep): Boolean = when (step) {
        is OnboardingStep.Move -> mobController.player.controlVector.x != 0f
        is OnboardingStep.Jump -> mobController.player.velocity.y < JUMP_VELOCITY_THRESHOLD
        is OnboardingStep.Aim -> {
            val baseline = cursorBaseline
            val player = mobController.player
            baseline != null && (player.aimToPlayer != baseline)
        }

        is OnboardingStep.Break -> mobController.player.isHittingWithDamage
        is OnboardingStep.Place -> placeTriggered
        is OnboardingStep.OpenInventory -> gameWindowsManager.currentWindowType in inventoryWindowTypes
    }

    companion object {
        private const val COOLDOWN_SEC = 1.5f
        private const val JUMP_VELOCITY_THRESHOLD = -3f

        private val inventoryWindowTypes = setOf(
            GameWindowType.CREATIVE_INVENTORY,
            GameWindowType.CREATIVE_INVENTORY_TABS,
            GameWindowType.SURVIVAL_INVENTORY,
        )
    }
}
