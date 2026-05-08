package ru.fredboy.cavedroid.gdx.menu.v2.view.death

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class DeathScreenViewModel(
    private val applicationController: ApplicationController,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    fun onRespawnClick() {
        applicationController.respawnPlayer()
    }

    fun onBackToMenuClick() {
        applicationController.quitGame()
    }
}
