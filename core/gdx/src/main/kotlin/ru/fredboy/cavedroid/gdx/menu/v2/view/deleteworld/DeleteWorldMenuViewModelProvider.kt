package ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class DeleteWorldMenuViewModelProvider @Inject constructor(
    private val saveDataRepository: SaveDataRepository,
    private val applicationContextRepository: ApplicationContextRepository,
    private val fontAssetsRepository: FontTextureAssetsRepository,
) : ViewModelProvider<DeleteWorldMenuNavKey, DeleteWorldMenuViewModel> {

    override val viewModelClass: KClass<DeleteWorldMenuViewModel>
        get() = DeleteWorldMenuViewModel::class

    override fun get(navKey: DeleteWorldMenuNavKey, navBackStack: NavBackStack): DeleteWorldMenuViewModel {
        return DeleteWorldMenuViewModel(
            saveDataRepository = saveDataRepository,
            applicationContextRepository = applicationContextRepository,
            navBackStack = navBackStack,
            saveDirectory = navKey.saveDirectory,
            worldName = navKey.worldName,
            fontAssetsRepository = fontAssetsRepository,
        )
    }
}
