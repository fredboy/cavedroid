package ru.fredboy.cavedroid.gdx.menu.v2.view.editworld

import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.api.SaveTransferController
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class EditWorldMenuViewModelProvider @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val saveDataRepository: SaveDataRepository,
    private val saveTransferController: SaveTransferController,
    private val inlineTextInput: InlineTextInput,
    private val softKeyboardObserver: SoftKeyboardObserver,
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<EditWorldMenuNavKey, EditWorldMenuViewModel> {

    override val viewModelClass: KClass<EditWorldMenuViewModel>
        get() = EditWorldMenuViewModel::class

    override fun get(navKey: EditWorldMenuNavKey, navBackStack: NavBackStack): EditWorldMenuViewModel {
        return EditWorldMenuViewModel(
            applicationContextRepository = applicationContextRepository,
            saveDataRepository = saveDataRepository,
            saveTransferController = saveTransferController,
            inlineTextInput = inlineTextInput,
            softKeyboardObserver = softKeyboardObserver,
            navBackStack = navBackStack,
            saveDirectory = navKey.saveDirectory,
            baseViewModelDependencies = baseViewModelDependencies,
        )
    }
}
