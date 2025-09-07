package ru.fredboy.cavedroid.gdx.menu.v2.view.help

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class HelpMenuViewModelProvider @Inject constructor(
    private val fontAssetsRepository: FontAssetsRepository,
) : ViewModelProvider<HelpMenuNavKey, HelpMenuViewModel> {

    override val viewModelClass: KClass<HelpMenuViewModel>
        get() = HelpMenuViewModel::class

    override fun get(navKey: HelpMenuNavKey, navBackStack: NavBackStack): HelpMenuViewModel {
        return HelpMenuViewModel(navBackStack, fontAssetsRepository)
    }
}
