package ru.fredboy.cavedroid.common.mvvm

import kotlin.reflect.KClass

interface ViewModelProvider<K : NavKey, V : ViewModel> {

    val viewModelClass: KClass<V>

    fun get(navKey: K, navBackStack: NavBackStack): V
}
