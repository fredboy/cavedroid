package ru.fredboy.cavedroid.common.mvvm

internal interface NavStageHost {
    fun onStackChanged(topKey: NavKey, poppedKey: NavKey? = null)

    fun clearViewModelFor(navKey: NavKey)
}
