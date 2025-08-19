package ru.fredboy.cavedroid.gdx.menu.v2.navigation

import kotlin.reflect.KClass

interface ViewModelProvider<ViewModel : Any> {

    val viewModelClass: KClass<ViewModel>

    fun get(navKey: NavKey, navBackStack: NavBackStack): ViewModel
}
