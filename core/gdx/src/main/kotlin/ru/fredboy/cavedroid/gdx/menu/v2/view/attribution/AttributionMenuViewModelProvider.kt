package ru.fredboy.cavedroid.gdx.menu.v2.view.attribution

import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.BindViewModelProvider
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModelProvider
import javax.inject.Inject
import kotlin.reflect.KClass

@MenuScope
@BindViewModelProvider
class AttributionMenuViewModelProvider @Inject constructor() :
    ViewModelProvider<
        AttributionMenuNavKey,
        AttributionMenuViewModel,
        > {

    override val viewModelClass: KClass<AttributionMenuViewModel>
        get() = AttributionMenuViewModel::class

    override fun get(navKey: AttributionMenuNavKey, navBackStack: NavBackStack): AttributionMenuViewModel {
        return AttributionMenuViewModel(navBackStack)
    }
}
