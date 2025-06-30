package ru.fredboy.cavedroid.data.configuration.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.configuration.repository.GameContextRepositoryImpl
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository

@Module
abstract class GameContextModule {

    @Binds
    internal abstract fun bindGameContextRepository(impl: GameContextRepositoryImpl): GameContextRepository
}