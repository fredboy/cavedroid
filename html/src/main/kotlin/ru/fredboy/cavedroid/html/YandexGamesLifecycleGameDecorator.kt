package ru.fredboy.cavedroid.html

import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import ru.fredboy.cavedroid.gdx.CaveDroidApplicationDecorator
import ru.fredboy.cavedroid.gdx.game.GameScreen

class YandexGamesLifecycleGameDecorator(
    private val delegate: CaveDroidApplication,
) : CaveDroidApplicationDecorator by delegate {

    override fun create() {
        delegate.applicationControllerOverride = this
        delegate.create()
        YandexGamesBridge.notifyLoadingReady()

        YandexGamesBridge.listenGameApiResume {
            if (delegate.screen is GameScreen) {
                YandexGamesBridge.notifyGameplayStart()
            }

            delegate.applicationComponentOrNull?.soundPlayer?.resumeAll()
        }

        YandexGamesBridge.listenGameApiPause {
            if (delegate.screen is GameScreen) {
                pauseGame()
            }

            delegate.applicationComponentOrNull?.soundPlayer?.pauseAll()
        }
    }

    override fun pauseGame() {
        delegate.pauseGame()
        YandexGamesBridge.notifyGameplayStop()
        YandexGamesBridge.showBanner()
    }

    override fun resumeGame() {
        delegate.resumeGame()
        YandexGamesBridge.notifyGameplayStart()
        YandexGamesBridge.hideBanner()
    }

    override fun startGame(startGameConfig: StartGameConfig) {
        delegate.startGame(startGameConfig)
        YandexGamesBridge.notifyGameplayStart()
        YandexGamesBridge.hideBanner()
    }

    override fun saveGame() {
        delegate.saveGame()
        YandexGamesBridge.notifyGameplayStart()
        YandexGamesBridge.hideBanner()
    }

    override fun quitGame() {
        YandexGamesBridge.notifyGameplayStop()
        delegate.quitGame()
        YandexGamesBridge.showBanner()
    }

    override fun showDeathScreen() {
        delegate.showDeathScreen()
        YandexGamesBridge.showBanner()
    }

    override fun respawnPlayer() {
        delegate.respawnPlayer()
        YandexGamesBridge.hideBanner()
    }
}
