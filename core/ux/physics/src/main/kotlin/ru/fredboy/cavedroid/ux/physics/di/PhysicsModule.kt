package ru.fredboy.cavedroid.ux.physics.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.game.world.abstraction.GamePhysicsController
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import ru.fredboy.cavedroid.ux.physics.impl.GamePhysicsControllerImpl
import ru.fredboy.cavedroid.ux.physics.impl.GameWorldSolidBlockBodiesManagerImpl

@Module
abstract class PhysicsModule {

    @Binds
    internal abstract fun bindGamePhysics(impl: GamePhysicsControllerImpl): GamePhysicsController

    @Binds
    internal abstract fun bindGameWorldSolidBlockBodiesManager(
        impl: GameWorldSolidBlockBodiesManagerImpl,
    ): GameWorldSolidBlockBodiesManager
}
