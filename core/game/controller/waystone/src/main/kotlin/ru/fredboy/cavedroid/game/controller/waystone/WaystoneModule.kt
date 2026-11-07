package ru.fredboy.cavedroid.game.controller.waystone

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravel

@Module
abstract class WaystoneModule {
    @Binds
    abstract fun bindWaystoneTravel(impl: WaystoneTravelImpl): WaystoneTravel
}
