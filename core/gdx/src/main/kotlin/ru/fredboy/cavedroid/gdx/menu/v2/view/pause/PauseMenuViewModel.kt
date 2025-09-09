package ru.fredboy.cavedroid.gdx.menu.v2.view.pause

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey

class PauseMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

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
