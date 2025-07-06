package ru.fredboy.cavedroid.ux.physics.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.game.world.abstraction.GamePhysicsController
import ru.fredboy.cavedroid.ux.physics.impl.GamePhysics

@Module
abstract class PhysicsModule {

    @Binds
    internal abstract fun bindGamePhysics(impl: GamePhysics): GamePhysicsController
}
