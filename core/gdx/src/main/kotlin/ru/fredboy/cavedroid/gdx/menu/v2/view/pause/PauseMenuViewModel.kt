package ru.fredboy.cavedroid.gdx.menu.v2.view.pause

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.stats.StatsMenuNavKey

class PauseMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    fun onResumeClick() {
        applicationController.resumeGame()
    }

    fun onSaveClick() {
        applicationController.saveGame()
    }

    fun onSettingsClick() {
        navBackStack.push(SettingsMenuNavKey)
    }

    fun onStatisticsClick() {
        navBackStack.push(StatsMenuNavKey)
    }

    fun onQuitGameClick() {
        applicationController.quitGame()
    }
}
