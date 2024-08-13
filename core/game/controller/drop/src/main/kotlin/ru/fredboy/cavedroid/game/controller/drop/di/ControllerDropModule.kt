package ru.fredboy.cavedroid.game.controller.drop.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.drop.impl.DropControllerImpl

@Module
abstract class ControllerDropModule {

    @Binds
    internal abstract fun bindDropController(impl: DropControllerImpl): DropController

}