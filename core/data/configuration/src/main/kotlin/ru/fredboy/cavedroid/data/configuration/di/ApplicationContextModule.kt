package ru.fredboy.cavedroid.data.configuration.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.configuration.repository.ApplicationContextRepositoryImpl
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository

@Module
abstract class ApplicationContextModule {

    @Binds
    internal abstract fun bindApplicationContextRepository(impl: ApplicationContextRepositoryImpl): ApplicationContextRepository
}