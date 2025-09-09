package ru.fredboy.cavedroid.gdx.menu.v2.view.language

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class LanguageMenuViewModelProvider @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<LanguageMenuNavKey, LanguageMenuViewModel> {

    override val viewModelClass: KClass<LanguageMenuViewModel>
        get() = LanguageMenuViewModel::class

    override fun get(navKey: LanguageMenuNavKey, navBackStack: NavBackStack): LanguageMenuViewModel {
        return LanguageMenuViewModel(
            applicationContextRepository = applicationContextRepository,
            navBackStack = navBackStack,
            baseViewModelDependencies = baseViewModelDependencies,
        )
    }
}
