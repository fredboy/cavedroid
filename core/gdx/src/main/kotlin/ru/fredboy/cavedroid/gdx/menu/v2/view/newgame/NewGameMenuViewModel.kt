package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class NewGameMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
) : ViewModel() {

    fun onSurvivalClick() {
        applicationController.newGameSurvival()
    }

    fun onCreativeClick() {
        applicationController.newGameCreative()
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
