package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class SettingsMenuViewModelProvider @Inject constructor(
    private val applicationController: ApplicationController,
    private val applicationContextRepository: ApplicationContextRepository,
    private val fontAssetsRepository: FontAssetsRepository,
) : ViewModelProvider<SettingsMenuNavKey, SettingsMenuViewModel> {

    override val viewModelClass: KClass<SettingsMenuViewModel>
        get() = SettingsMenuViewModel::class

    override fun get(navKey: SettingsMenuNavKey, navBackStack: NavBackStack): SettingsMenuViewModel {
        return SettingsMenuViewModel(
            applicationController = applicationController,
            navBackStack = navBackStack,
            applicationContextRepository = applicationContextRepository,
            fontAssetsRepository = fontAssetsRepository,
        )
    }
}
