package ru.fredboy.cavedroid.game.controller.container.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.container.impl.ContainerControllerImpl

@Module
abstract class ControllerContainerModule {

    @Binds
    internal abstract fun bindContainerController(impl: ContainerControllerImpl): ContainerController

}