package ru.fredboy.cavedroid.data.items.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.items.repository.ItemsRepositoryImpl
import ru.fredboy.cavedroid.data.items.repository.MobParamsRepositoryImpl
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository

@Module
abstract class DataItemsModule {

    @Binds
    internal abstract fun bindItemsRepository(impl: ItemsRepositoryImpl): ItemsRepository

    @Binds
    internal abstract fun bindMobParamsRepository(impl: MobParamsRepositoryImpl): MobParamsRepository
}
