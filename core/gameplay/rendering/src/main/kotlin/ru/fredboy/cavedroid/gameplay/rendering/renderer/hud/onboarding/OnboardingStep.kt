package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

sealed class OnboardingStep(
    val touchKey: String?,
    val kbmKey: String?,
) {

    fun isAvailable(isTouch: Boolean): Boolean = if (isTouch) touchKey != null else kbmKey != null

    fun localizationKey(isTouch: Boolean): String = requireNotNull(if (isTouch) touchKey else kbmKey)

    object Move : OnboardingStep(touchKey = "onboardingMoveTouch", kbmKey = "onboardingMoveKbm")

    object Jump : OnboardingStep(touchKey = "onboardingJumpTouch", kbmKey = "onboardingJumpKbm")

    object Aim : OnboardingStep(touchKey = "onboardingAim", kbmKey = null)

    object Break : OnboardingStep(touchKey = "onboardingBreakTouch", kbmKey = "onboardingBreakKbm")

    object Place : OnboardingStep(touchKey = "onboardingPlaceTouch", kbmKey = "onboardingPlaceKbm")

    object OpenInventory : OnboardingStep(touchKey = null, kbmKey = "onboardingOpenInventory")

    companion object {
        val ALL: List<OnboardingStep> = listOf(Move, Jump, Aim, Break, Place, OpenInventory)
    }
}
