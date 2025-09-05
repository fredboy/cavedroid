package ru.fredboy.cavedroid.gdx.menu.v2.view.main

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class MainMenuViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
) : ViewModelProvider<MainMenuNavKey, MainMenuViewModel> {

    override val viewModelClass: KClass<MainMenuViewModel>
        get() = MainMenuViewModel::class

    override fun get(navKey: MainMenuNavKey, navBackStack: NavBackStack): MainMenuViewModel {
        return MainMenuViewModel(applicationController, navBackStack)
    }
}
