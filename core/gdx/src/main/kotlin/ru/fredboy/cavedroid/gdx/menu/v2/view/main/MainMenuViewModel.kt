package ru.fredboy.cavedroid.gdx.menu.v2.view.main

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import ru.fredboy.cavedroid.gdx.menu.v2.view.disclaimer.AdsDisclaimerNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.help.HelpMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.language.LanguageMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.settings.SettingsMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer.SinglePlayerMenuNavKey

class MainMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
    baseViewModelDependencies: BaseViewModelDependencies,
    private val adController: AdController,
    private val applicationContextRepository: ApplicationContextRepository,
) : BaseViewModel(baseViewModelDependencies) {

    val showExitButton: Boolean
        get() = Gdx.app.type != Application.ApplicationType.WebGL

    override fun onShow() {
        if (adController.supportsPersonalizedAdsConsent &&
            applicationContextRepository.getPersonalizedAdsConsent() == null
        ) {
            navBackStack.push(AdsDisclaimerNavKey)
            return
        }
        adController.showBanner()
    }

    override fun onHide() {
        adController.hideBanner()
    }

    override fun onDispose() {
        adController.hideBanner()
    }

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
