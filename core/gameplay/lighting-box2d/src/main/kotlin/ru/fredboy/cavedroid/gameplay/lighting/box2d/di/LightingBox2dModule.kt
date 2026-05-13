package ru.fredboy.cavedroid.gameplay.lighting.box2d.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import ru.fredboy.cavedroid.gameplay.lighting.box2d.Box2dLightingSystem

@Module
abstract class LightingBox2dModule {

    @Binds
    @GameScope
    internal abstract fun bindLightingSystem(impl: Box2dLightingSystem): LightingSystem
}
