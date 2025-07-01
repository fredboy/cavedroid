package ru.fredboy.cavedroid.game.world.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.game.world.impl.WorldAdapterImpl

@Module
abstract class GameWorldModule {

    @Binds
    internal abstract fun bindMobWorldAdapter(impl: WorldAdapterImpl): MobWorldAdapter

    @Binds
    internal abstract fun bindContainerWorldAdapter(impl: WorldAdapterImpl): ContainerWorldAdapter

    @Binds
    internal abstract fun bindDropWorldAdapter(impl: WorldAdapterImpl): DropWorldAdapter
}
