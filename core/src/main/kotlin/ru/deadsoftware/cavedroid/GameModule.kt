package ru.deadsoftware.cavedroid

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.common.api.GameController

@Module
abstract class GameModule {

    @Binds
    internal abstract fun bindGameController(impl: CaveGame): GameController

}