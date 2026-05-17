package ru.fredboy.cavedroid.data.stats.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.stats.repository.StatsRepositoryImpl
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository

@Module
abstract class DataStatsModule {

    @Binds
    internal abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository
}
