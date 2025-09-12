package ru.fredboy.cavedroid.game.controller.mob.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.mob.factory.MobFactoryImpl
import ru.fredboy.cavedroid.game.controller.mob.impl.MobPhysicsFactoryImpl
import ru.fredboy.cavedroid.game.controller.mob.impl.PlayerAdapterImpl

@Module
abstract class MobControllerModule {

    @Binds
    internal abstract fun bindPlayerAdapter(impl: PlayerAdapterImpl): PlayerAdapter

    @Binds
    internal abstract fun bindMobPhysicsFactory(impl: MobPhysicsFactoryImpl): MobPhysicsFactory

    @Binds
    internal abstract fun bindMobFactory(impl: MobFactoryImpl): MobFactory
}
