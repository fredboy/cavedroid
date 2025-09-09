package ru.fredboy.cavedroid.gdx.menu.v2.view.pause

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class PauseMenuViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<PauseMenuNavKey, PauseMenuViewModel> {

    override val viewModelClass: KClass<PauseMenuViewModel>
        get() = PauseMenuViewModel::class

    override fun get(navKey: PauseMenuNavKey, navBackStack: NavBackStack): PauseMenuViewModel {
        return PauseMenuViewModel(applicationController, navBackStack, baseViewModelDependencies)
    }
}
