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
    }

    override fun resumeGame() {
        delegate.resumeGame()
        YandexGamesBridge.notifyGameplayStart()
    }

    override fun startGame(startGameConfig: StartGameConfig) {
        delegate.startGame(startGameConfig)
        YandexGamesBridge.notifyGameplayStart()
    }

    override fun quitGame() {
        YandexGamesBridge.notifyGameplayStop()
        delegate.quitGame()
    }
}
