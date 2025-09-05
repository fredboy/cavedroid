package ru.fredboy.cavedroid.gdx.menu.v2.view.pause

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey

class PauseMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
) : ViewModel() {

    fun onResumeClick() {
        applicationController.resumeGame()
    }

    fun onSettingsClick() {
        navBackStack.push(SettingsMenuNavKey)
    }

    fun onQuitGameClick() {
        applicationController.quitGame()
    }
}
