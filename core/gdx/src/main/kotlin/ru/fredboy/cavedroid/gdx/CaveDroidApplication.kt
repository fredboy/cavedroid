package ru.fredboy.cavedroid.gdx

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.common.utils.DEFAULT_VIEWPORT_WIDTH
import ru.fredboy.cavedroid.common.utils.ratio
import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent
import ru.fredboy.cavedroid.gdx.di.DaggerApplicationComponent
import ru.fredboy.cavedroid.gdx.game.GameScreen
import ru.fredboy.cavedroid.gdx.menu.v2.PauseMenuScreen
import java.util.Locale

class CaveDroidApplication(
    private val gameDataDirectoryPath: String,
    private val isTouchScreen: Boolean,
    private val isDebug: Boolean,
    private val preferencesStore: PreferencesStore,
) : Game(),
    ApplicationController {

    lateinit var applicationComponent: ApplicationComponent
        private set

    private fun initFullscreenMode(isFullscreen: Boolean) {
        if (Gdx.app.type != Application.ApplicationType.Desktop) {
            return
        }

        if (isFullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else {
            Gdx.graphics.setWindowedMode(960, 540)
        }
    }

    override fun create() {
        val width = DEFAULT_VIEWPORT_WIDTH
        val height = width / Gdx.graphics.ratio

        val isFullscreen = preferencesStore.getPreference("fullscreen").toBoolean()
        initFullscreenMode(isFullscreen)

        applicationComponent = DaggerApplicationComponent.builder()
            .applicationContext(
                ApplicationContext(
                    isDebug = isDebug,
                    isTouch = isTouchScreen,
                    gameDirectory = gameDataDirectoryPath,
                    width = width,
                    height = height,
                    isFullscreen = isFullscreen,
                    useDynamicCamera = preferencesStore.getPreference("dyncam").toBoolean(),
                    isAutoJumpEnabled = preferencesStore.getPreference("auto_jump").toBoolean(),
                    locale = Locale(preferencesStore.getPreference("locale") ?: Locale.getDefault().language),
                ),
            )
            .applicationController(this)
            .preferencesStore(preferencesStore)
            .build()

        Gdx.app.logLevel = Application.LOG_DEBUG

        Gdx.files.absolute(gameDataDirectoryPath).mkdirs()
        applicationComponent.initializeAssets()
        setScreen(applicationComponent.menuScreen)
    }

    override fun dispose() {
        applicationComponent.menuScreen.dispose()
        applicationComponent.pauseMenuScreen.dispose()
        applicationComponent.gameScreen.dispose()
        applicationComponent.disposeAssets()
    }

    override fun quitGame() {
        applicationComponent.gameScreen.saveGame()
        applicationComponent.gameScreen.dispose()
        applicationComponent.pauseMenuScreen.dispose()
        setScreen(applicationComponent.menuScreen)
    }

    override fun startGame(startGameConfig: StartGameConfig) {
        val gameScreen = applicationComponent.gameScreen.apply {
            when (startGameConfig) {
                is StartGameConfig.New -> newGame(startGameConfig)
                is StartGameConfig.Load -> loadGame(startGameConfig)
            }
        }

        setScreen(gameScreen)
    }

    override fun exitGame() {
        setScreen(null)
        Gdx.app.exit()
    }

    override fun triggerResize() {
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun pauseGame() {
        if (screen !is GameScreen) {
            Gdx.app.error(TAG, "Cannot pause when active screen is not game")
            return
        }
        screen.pause()
        setScreen(applicationComponent.pauseMenuScreen)
    }

    override fun resumeGame() {
        if (screen !is PauseMenuScreen) {
            Gdx.app.error(TAG, "Cannot resume when active screen is not pause menu")
            return
        }
        setScreen(applicationComponent.gameScreen)
        screen.resume()
    }

    companion object {
        private const val TAG = "CaveDroidApplication"
    }
}
