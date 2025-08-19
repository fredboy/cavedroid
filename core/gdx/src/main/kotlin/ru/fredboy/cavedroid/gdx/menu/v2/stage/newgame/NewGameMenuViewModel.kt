package ru.fredboy.cavedroid.gdx.menu.v2.stage.newgame

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import javax.inject.Inject

class NewGameMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
) {

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
