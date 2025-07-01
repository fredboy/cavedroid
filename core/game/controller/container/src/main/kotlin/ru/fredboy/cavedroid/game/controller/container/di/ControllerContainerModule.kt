package ru.fredboy.cavedroid.game.controller.container.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.game.controller.container.impl.ContainerFactoryImpl

@Module
abstract class ControllerContainerModule {

    @Binds
    internal abstract fun bindContainerFactory(impl: ContainerFactoryImpl): ContainerFactory
}
