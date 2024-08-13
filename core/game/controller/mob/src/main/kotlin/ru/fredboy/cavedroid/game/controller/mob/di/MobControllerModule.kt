package ru.fredboy.cavedroid.game.controller.mob.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.impl.MobControllerImpl

@Module
abstract class MobControllerModule {

    @Binds
    internal abstract fun bindMobController(impl: MobControllerImpl): MobController

}