package ru.fredboy.cavedroid.gdx.menu.v2.view.about

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class AboutMenuViewModelProvider @Inject constructor(
    private val baseViewModelDependencies: BaseViewModelDependencies,
) : ViewModelProvider<AboutMenuNavKey, AboutMenuViewModel> {

    override val viewModelClass: KClass<AboutMenuViewModel>
        get() = AboutMenuViewModel::class

    override fun get(navKey: AboutMenuNavKey, navBackStack: NavBackStack): AboutMenuViewModel {
        return AboutMenuViewModel(navBackStack, baseViewModelDependencies)
    }
}
