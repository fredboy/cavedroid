package ru.fredboy.cavedroid.data.configuration.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.configuration.repository.GameConfigurationRepositoryImpl
import ru.fredboy.cavedroid.domain.configuration.repository.GameConfigurationRepository

@Module
abstract class DataConfigurationModule {

    @Binds
    internal abstract fun bindGameConfigurationRepository(impl: GameConfigurationRepositoryImpl): GameConfigurationRepository

}