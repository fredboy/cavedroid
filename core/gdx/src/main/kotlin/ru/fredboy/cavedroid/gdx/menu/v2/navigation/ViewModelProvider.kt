package ru.fredboy.cavedroid.gdx.menu.v2.navigation

import kotlin.reflect.KClass

interface ViewModelProvider<V : ViewModel> {

    val viewModelClass: KClass<V>

    fun get(navKey: NavKey, navBackStack: NavBackStack): V
}
