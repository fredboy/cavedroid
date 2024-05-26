package ru.deadsoftware.cavedroid

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import ru.deadsoftware.cavedroid.misc.utils.ratio
import ru.deadsoftware.cavedroid.prefs.PreferencesStore

class CaveGame(
    private val gameDataDirectoryPath: String,
    private val isTouchScreen: Boolean,
    private val isDebug: Boolean,
    private val preferencesStore: PreferencesStore,
) : Game() {

    private val mainComponent: MainComponent
    private val mainConfig: MainConfig

    private val assetLoader: AssetLoader

    init {
        mainComponent = DaggerMainComponent.builder()
            .caveGame(this)
            .preferencesStore(preferencesStore)
            .build()

        mainConfig = mainComponent.mainConfig
        assetLoader = mainComponent.assetLoader
    }

    private fun initMainConfig() {
        val width = DEFAULT_VIEWPORT_WIDTH
        val height = width / Gdx.graphics.ratio

        mainConfig.mainComponent = mainComponent
        mainConfig.gameFolder = gameDataDirectoryPath
        mainConfig.isTouch = isTouchScreen
        mainConfig.width = width
        mainConfig.height = height
        mainConfig.isShowInfo = isDebug

        Gdx.app.logLevel = if (isDebug) Application.LOG_DEBUG else Application.LOG_ERROR

        mainConfig.setFullscreenToggleListener { isFullscreen ->
            if (isFullscreen) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode);
            } else {
                Gdx.graphics.setWindowedMode(width.toInt(), height.toInt());
            }
        }
    }

    fun newGame(gameMode: Int) {
        setScreen(mainComponent.gameScreen.apply { newGame(gameMode) })
    }

    fun loadGame() {
        setScreen(mainComponent.gameScreen.apply { loadGame() })
    }

    fun quitGame() {
        screen?.dispose()
        setScreen(mainComponent.menuScreen)
    }

    override fun create() {
        Gdx.files.absolute(gameDataDirectoryPath).mkdirs()
        initMainConfig()

        mainComponent.initializeAssetsUseCase()
        setScreen(mainComponent.menuScreen)
    }

    override fun dispose() {
        screen?.dispose()
        mainComponent.disposeAssetsUseCase()
    }


    companion object {
        private const val TAG = "CaveGame"
        private const val DEFAULT_VIEWPORT_WIDTH = 480f

        const val VERSION = "alpha 0.9.2"
    }

}