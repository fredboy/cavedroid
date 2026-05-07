package ru.fredboy.cavedroid.gdx.game.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.common.api.OnboardingEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding.OnboardingController

@Module
abstract class OnboardingModule {

    @Binds
    @GameScope
    abstract fun bindOnboardingEvents(impl: OnboardingController): OnboardingEvents
}
