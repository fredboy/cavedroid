package ru.fredboy.cavedroid.gameplay.physics.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import ru.fredboy.cavedroid.gameplay.physics.impl.ChunkedGameWorldSolidBlockBodiesManagerImpl

@Module
abstract class PhysicsModule {

    @Binds
    internal abstract fun bindGameWorldSolidBlockBodiesManager(
        impl: ChunkedGameWorldSolidBlockBodiesManagerImpl,
    ): GameWorldSolidBlockBodiesManager
}
