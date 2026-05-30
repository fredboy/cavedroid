package ru.fredboy.cavedroid.gdx.menu.v2.view.worldconfig

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.common.utils.WorldNameSanitizer
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class WorldConfigMenuViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
    private val worldNameSanitizer: WorldNameSanitizer,
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<WorldConfigMenuNavKey, WorldConfigMenuViewModel> {

    override val viewModelClass: KClass<WorldConfigMenuViewModel>
        get() = WorldConfigMenuViewModel::class

    override fun get(navKey: WorldConfigMenuNavKey, navBackStack: NavBackStack): WorldConfigMenuViewModel {
        return WorldConfigMenuViewModel(
            applicationController = applicationController,
            worldNameSanitizer = worldNameSanitizer,
            navBackStack = navBackStack,
            worldName = navKey.worldName,
            gameMode = navKey.gameMode,
            seed = navKey.seed,
            baseViewModelDependencies = baseViewModelDependencies,
        )
    }
}
