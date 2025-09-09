package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.utils.WorldNameSanitizer
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class NewGameMenuViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
    private val worldNameSanitizer: WorldNameSanitizer,
    private val fontAssetsRepository: FontTextureAssetsRepository,
) : ViewModelProvider<NewGameMenuNavKey, NewGameMenuViewModel> {

    override val viewModelClass: KClass<NewGameMenuViewModel>
        get() = NewGameMenuViewModel::class

    override fun get(navKey: NewGameMenuNavKey, navBackStack: NavBackStack): NewGameMenuViewModel {
        return NewGameMenuViewModel(applicationController, worldNameSanitizer, navBackStack, fontAssetsRepository)
    }
}
