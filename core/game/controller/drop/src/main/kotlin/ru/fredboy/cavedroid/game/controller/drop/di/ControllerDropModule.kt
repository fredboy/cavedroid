package ru.fredboy.cavedroid.game.controller.drop.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.game.controller.drop.impl.DropAdapterImpl

@Module
abstract class ControllerDropModule {

    @Binds
    internal abstract fun bindDropAdapter(impl: DropAdapterImpl): DropAdapter

}