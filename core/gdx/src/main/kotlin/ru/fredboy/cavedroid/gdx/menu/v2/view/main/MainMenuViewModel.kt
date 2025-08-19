package ru.fredboy.cavedroid.gdx.menu.v2.view.main

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey

class MainMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
) : ViewModel() {

    fun onNewGameClick() {
        navBackStack.push(NewGameMenuNavKey)
    }

    fun onLoadGameClick() {
        applicationController.loadGame()
    }

    fun onSettingsClick() {
        navBackStack.push(SettingsMenuNavKey)
    }

    fun onExitGameClick() {
        applicationController.exitGame()
    }
}
