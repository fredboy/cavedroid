package ru.fredboy.cavedroid.gdx.menu.v2.view.death

import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class DeathScreenViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
    private val baseViewModelDependencies: BaseViewModelDependencies,
    private val adController: AdController,
) : ViewModelProvider<DeathScreenNavKey, DeathScreenViewModel> {

    override val viewModelClass: KClass<DeathScreenViewModel>
        get() = DeathScreenViewModel::class

    override fun get(navKey: DeathScreenNavKey, navBackStack: NavBackStack): DeathScreenViewModel {
        return DeathScreenViewModel(applicationController, baseViewModelDependencies, adController)
    }
}
