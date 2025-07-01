package ru.fredboy.cavedroid.game.controller.mob.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.mob.impl.PlayerAdapterImpl

@Module
abstract class MobControllerModule {

    @Binds
    internal abstract fun bindPlayerAdapter(impl: PlayerAdapterImpl): PlayerAdapter
}