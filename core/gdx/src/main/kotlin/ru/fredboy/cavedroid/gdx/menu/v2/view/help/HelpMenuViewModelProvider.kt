package ru.fredboy.cavedroid.gdx.menu.v2.view.help

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.mvvm.ViewModelProvider
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.di.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class HelpMenuViewModelProvider @Inject constructor(
    private val baseViewModelDependencies: BaseViewModelDependencies,
    private val applicationContextRepository: ApplicationContextRepository,
) : ViewModelProvider<HelpMenuNavKey, HelpMenuViewModel> {

    override val viewModelClass: KClass<HelpMenuViewModel>
        get() = HelpMenuViewModel::class

    override fun get(navKey: HelpMenuNavKey, navBackStack: NavBackStack): HelpMenuViewModel {
        return HelpMenuViewModel(navBackStack, baseViewModelDependencies, applicationContextRepository)
    }
}
