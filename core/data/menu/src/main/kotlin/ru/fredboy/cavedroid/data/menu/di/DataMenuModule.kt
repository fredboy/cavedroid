package ru.fredboy.cavedroid.data.menu.di

import dagger.Binds
import dagger.Module
import ru.fredboy.cavedroid.data.menu.repository.MenuButtonRepositoryImpl
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository

@Module
abstract class DataMenuModule {

    @Binds
    internal abstract fun bindMenuButtonsRepository(impl: MenuButtonRepositoryImpl): MenuButtonRepository

}
