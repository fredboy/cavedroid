package ru.fredboy.cavedroid.gdx

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.common.utils.ratio
import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent
import ru.fredboy.cavedroid.gdx.di.DaggerApplicationComponent
import ru.fredboy.cavedroid.gdx.game.GameScreen

class CaveDroidApplication(
    private val gameDataDirectoryPath: String,
    private val isTouchScreen: Boolean,
    private val isDebug: Boolean,
    private val preferencesStore: PreferencesStore,
) : Game(),
    ApplicationController {

    lateinit var applicationComponent: ApplicationComponent
        private set

    private fun newGame(gameMode: Int) {
        setScreen(applicationComponent.gameScreen.apply { newGame(gameMode) })
    }

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
                    screenScale = (preferencesStore.getPreference("screen_scale") ?: "5").toInt(),
                    isAutoJumpEnabled = preferencesStore.getPreference("auto_jump").toBoolean(),
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
        applicationComponent.gameScreen.dispose()
        applicationComponent.disposeAssets()
    }

    override fun quitGame() {
        (screen as? GameScreen)?.let { gameScreen ->
            gameScreen.dispose()
            setScreen(applicationComponent.menuScreen)
        } ?: Gdx.app.error(TAG, "quitGame called when active screen is not Game")
    }

    override fun newGameCreative() {
        newGame(1)
    }

    override fun newGameSurvival() {
        newGame(0)
    }

    override fun loadGame() {
        setScreen(applicationComponent.gameScreen.apply { loadGame() })
    }

    override fun exitGame() {
        setScreen(null)
        Gdx.app.exit()
    }

    override fun triggerResize() {
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    companion object {
        private const val TAG = "CaveDroidApplication"
        private const val DEFAULT_VIEWPORT_WIDTH = 480f
    }
}
