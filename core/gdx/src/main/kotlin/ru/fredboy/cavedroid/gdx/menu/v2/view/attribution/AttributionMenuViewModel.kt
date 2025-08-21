package ru.fredboy.cavedroid.gdx.menu.v2.view.attribution

import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class AttributionMenuViewModel(
    private val navBackStack: NavBackStack,
) : ViewModel() {

    fun onBackClick() {
        navBackStack.pop()
    }
}
