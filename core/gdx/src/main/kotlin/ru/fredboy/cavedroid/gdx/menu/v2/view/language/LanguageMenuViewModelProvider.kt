package ru.fredboy.cavedroid.gdx.menu.v2.view.language

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
class LanguageMenuViewModelProvider @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val fontAssetsRepository: FontAssetsRepository,
) : ViewModelProvider<LanguageMenuNavKey, LanguageMenuViewModel> {

    override val viewModelClass: KClass<LanguageMenuViewModel>
        get() = LanguageMenuViewModel::class

    override fun get(navKey: LanguageMenuNavKey, navBackStack: NavBackStack): LanguageMenuViewModel {
        return LanguageMenuViewModel(
            applicationContextRepository = applicationContextRepository,
            navBackStack = navBackStack,
            fontAssetsRepository = fontAssetsRepository,
        )
    }
}
