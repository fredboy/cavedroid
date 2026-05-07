package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

data class OnboardingState(
    val step: OnboardingStep,
    val awaitingInput: Boolean,
    val cooldownProgress: Float,
    val isLast: Boolean,
)
