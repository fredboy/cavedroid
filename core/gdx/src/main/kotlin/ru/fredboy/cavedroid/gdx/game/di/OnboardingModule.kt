package ru.fredboy.cavedroid.gdx.game.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.common.api.GameMessageEvents
import ru.fredboy.cavedroid.common.api.InventoryHintEvents
import ru.fredboy.cavedroid.common.api.OnboardingEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages.GameMessageController
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages.InventoryHintController
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding.OnboardingController

@Module
abstract class OnboardingModule {

    @Binds
    @GameScope
    abstract fun bindOnboardingEvents(impl: OnboardingController): OnboardingEvents

    @Binds
    @GameScope
    abstract fun bindGameMessageEvents(impl: GameMessageController): GameMessageEvents

    @Binds
    @GameScope
    abstract fun bindInventoryHintEvents(impl: InventoryHintController): InventoryHintEvents
}
