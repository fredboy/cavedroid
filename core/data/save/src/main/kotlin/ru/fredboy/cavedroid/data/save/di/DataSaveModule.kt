package ru.fredboy.cavedroid.data.save.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.save.repository.SaveDataRepositoryImpl
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository

@Module
abstract class DataSaveModule {

    @Binds
    internal abstract fun bindSaveDataRepository(impl: SaveDataRepositoryImpl): SaveDataRepository

}
