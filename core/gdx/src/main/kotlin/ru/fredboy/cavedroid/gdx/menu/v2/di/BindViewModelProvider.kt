package ru.fredboy.cavedroid.gdx.menu.v2.di

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider

@BindsIntoSet(
    interfaceClass = ViewModelProvider::class,
    modulePackage = "ru.fredboy.cavedroid.gdx.menu.v2.generated",
    moduleName = "ViewModelProviderModule",
)
annotation class BindViewModelProvider
