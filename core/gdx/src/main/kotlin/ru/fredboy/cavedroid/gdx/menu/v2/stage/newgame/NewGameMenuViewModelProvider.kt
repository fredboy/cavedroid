package ru.fredboy.cavedroid.gdx.menu.v2.stage.newgame

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavKey
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class NewGameMenuViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
) : ViewModelProvider<NewGameMenuViewModel> {

    override val viewModelClass: KClass<NewGameMenuViewModel>
        get() = NewGameMenuViewModel::class

    override fun get(navKey: NavKey, navBackStack: NavBackStack): NewGameMenuViewModel {
        return NewGameMenuViewModel(applicationController, navBackStack)
    }
}
