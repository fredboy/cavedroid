package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class SinglePlayerMenuViewModelProvider @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val applicationController: ApplicationController,
    private val saveDataRepository: SaveDataRepository,
    private val fontAssetsRepository: FontAssetsRepository,
) : ViewModelProvider<SinglePlayerMenuNavKey, SinglePlayerMenuViewModel> {

    override val viewModelClass: KClass<SinglePlayerMenuViewModel>
        get() = SinglePlayerMenuViewModel::class

    override fun get(navKey: SinglePlayerMenuNavKey, navBackStack: NavBackStack): SinglePlayerMenuViewModel {
        return SinglePlayerMenuViewModel(
            applicationContextRepository = applicationContextRepository,
            applicationController = applicationController,
            saveDataRepository = saveDataRepository,
            navBackStack = navBackStack,
            fontAssetsRepository = fontAssetsRepository,
        )
    }
}
