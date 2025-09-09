package ru.fredboy.cavedroid.gdx.menu.v2.view.main

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.HelpMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.language.LanguageMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer.SinglePlayerMenuNavKey

class MainMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
    fontAssetsRepository: FontTextureAssetsRepository,
) : BaseViewModel(fontAssetsRepository) {

    fun onSinglePlayerClick() {
        navBackStack.push(SinglePlayerMenuNavKey)
    }

    fun onSettingsClick() {
        navBackStack.push(SettingsMenuNavKey)
    }

    fun onExitGameClick() {
        applicationController.exitGame()
    }

    fun onHelpClick() {
        navBackStack.push(HelpMenuNavKey)
    }

    fun onLanguageClick() {
        navBackStack.push(LanguageMenuNavKey)
    }
}
