package ru.fredboy.cavedroid.data.stats.di

import dagger.Module
import dagger.Provides
import ru.fredboy.cavedroid.data.stats.repository.StatsRepositoryImpl
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import javax.inject.Provider
import javax.inject.Singleton

@Module
class DataStatsModule(private val existing: StatsRepository? = null) {

    @Provides
    @Singleton
    internal fun provideStatsRepository(impl: Provider<StatsRepositoryImpl>): StatsRepository = existing ?: impl.get()
}
