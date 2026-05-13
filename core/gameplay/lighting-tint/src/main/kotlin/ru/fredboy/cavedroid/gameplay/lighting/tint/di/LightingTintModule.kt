package ru.fredboy.cavedroid.gameplay.lighting.tint.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.gameplay.lighting.tint.TintLightingSystem

@Module
abstract class LightingTintModule {

    @Binds
    @GameScope
    internal abstract fun bindLightingSystem(impl: TintLightingSystem): LightingSystem
}
