package ru.fredboy.cavedroid.gdx

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.CaveDroidConstants.PreferenceKeys
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
    CaveDroidApplicationDecorator,
    ApplicationController {

    override lateinit var applicationComponent: ApplicationComponent
        private set

    private fun initFullscreenMode(isFullscreen: Boolean) {
        if (Gdx.app.type != Application.ApplicationType.Desktop) {
            return
        }

        if (isFullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else {
            Gdx.graphics.setWindowedMode(
                preferencesStore.getPreference(PreferenceKeys.WINDOW_WIDTH_KEY)?.toIntOrNull() ?: 960,
                preferencesStore.getPreference(PreferenceKeys.WINDOW_HEIGHT_KEY)?.toIntOrNull() ?: 540,
            )
        }
    }

    override fun create() {
        val width = DEFAULT_VIEWPORT_WIDTH
        val height = width / Gdx.graphics.ratio

        val isFullscreen = preferencesStore.getPreference(PreferenceKeys.FULLSCREEN).toBoolean()
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
                    useDynamicCamera = preferencesStore.getPreference(PreferenceKeys.DYNAMIC_CAMERA).toBoolean(),
                    isAutoJumpEnabled = preferencesStore.getPreference(PreferenceKeys.AUTO_JUMP).toBoolean(),
                    locale = Locale(
                        preferencesStore.getPreference(PreferenceKeys.LOCALE)
                            ?: Locale.getDefault().language,
                    ),
                    soundEnabled = preferencesStore.getPreference(PreferenceKeys.SOUND_ENABLED).toBoolean(),
                ),
            )
            .applicationController(this)
            .preferencesStore(preferencesStore)
            .build()

        Gdx.app.logLevel = if (isDebug) {
            Application.LOG_DEBUG
        } else {
            Application.LOG_INFO
        }

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

    fun getPreferencesStore() = preferencesStore

    override fun getDelegate() = this

    companion object {
        private const val TAG = "CaveDroidApplication"
    }
}
