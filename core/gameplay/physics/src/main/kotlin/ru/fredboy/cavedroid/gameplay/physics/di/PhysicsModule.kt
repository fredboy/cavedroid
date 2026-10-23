package ru.fredboy.cavedroid.gameplay.physics.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.entity.container.abstraction.MobSpawnerAdapter
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import ru.fredboy.cavedroid.gameplay.physics.impl.ChunkedGameWorldSolidBlockBodiesManagerImpl
import ru.fredboy.cavedroid.gameplay.physics.impl.MobSpawnerAdapterImpl

@Module
abstract class PhysicsModule {

    @Binds
    internal abstract fun bindMobSpawnerAdapter(impl: MobSpawnerAdapterImpl): MobSpawnerAdapter

    @Binds
    internal abstract fun bindGameWorldSolidBlockBodiesManager(
        impl: ChunkedGameWorldSolidBlockBodiesManagerImpl,
    ): GameWorldSolidBlockBodiesManager
}
