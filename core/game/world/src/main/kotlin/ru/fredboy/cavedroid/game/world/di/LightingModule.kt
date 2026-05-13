package ru.fredboy.cavedroid.game.world.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.GameWorldLightManager
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem

@Module
abstract class LightingModule {

    @Binds
    @GameScope
    internal abstract fun bindLightingSystem(impl: GameWorldLightManager): LightingSystem
}
