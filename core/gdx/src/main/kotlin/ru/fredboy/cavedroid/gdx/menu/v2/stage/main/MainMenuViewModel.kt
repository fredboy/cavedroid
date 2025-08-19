package ru.fredboy.cavedroid.gdx.menu.v2.stage.main

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.stage.newgame.NewGameMenuNavKey

class MainMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
) {

    fun onNewGameClick() {
        navBackStack.push(NewGameMenuNavKey)
    }

    fun onLoadGameClick() {
    }

    fun onSettingsClick() {
    }

    fun onExitGameClick() {
        applicationController.exitGame()
    }
}
