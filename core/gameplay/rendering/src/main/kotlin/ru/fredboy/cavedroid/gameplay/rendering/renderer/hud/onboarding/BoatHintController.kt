package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

import ru.fredboy.cavedroid.common.api.BoatHintEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import javax.inject.Inject

@GameScope
class BoatHintController @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : BoatHintEvents {
    private var completed = applicationContextRepository.isBoatHintShown()
    private var boatId: String? = null

    var state: OnboardingState? = null
        private set

    private val enabled get() = applicationContextRepository.isTouch() && !completed

    override fun notifyPlacedAfloat(id: String) {
        if (!enabled || state?.step == OnboardingStep.DismountBoat) return
        boatId = id
        state = OnboardingState(OnboardingStep.MountBoat, awaitingInput = true, cooldownProgress = 0f, isLast = false)
    }

    override fun notifyMounted(id: String) {
        if (!enabled) return
        boatId = id
        state = OnboardingState(OnboardingStep.DismountBoat, awaitingInput = true, cooldownProgress = 0f, isLast = true)
    }

    override fun notifyDetached(dismounted: Boolean) {
        val current = state ?: return
        boatId = null
        if (dismounted && current.step == OnboardingStep.DismountBoat && current.awaitingInput) {
            completed = true
            applicationContextRepository.setBoatHintShown(true)
            state = current.copy(awaitingInput = false)
        } else {
            // Death, destruction and session cleanup do not teach the dismount gesture.
            state = null
        }
    }

    override fun notifyRemoved(id: String) {
        if (boatId != id) return
        boatId = null
        state = null
    }

    fun update(delta: Float) {
        val current = state ?: return
        if (current.awaitingInput) return
        val progress = current.cooldownProgress + delta / FADE_DURATION_SEC
        state = if (progress >= 1f) null else current.copy(cooldownProgress = progress)
    }

    companion object {
        private const val FADE_DURATION_SEC = 1.5f
    }
}
