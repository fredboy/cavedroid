package ru.deadsoftware.cavedroid

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import ru.deadsoftware.cavedroid.prefs.PreferencesStore
import ru.fredboy.cavedroid.common.utils.ratio
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository

class CaveGame(
    private val gameDataDirectoryPath: String,
    private val isTouchScreen: Boolean,
    private val isDebug: Boolean,
    private val preferencesStore: PreferencesStore,
) : BaseGame() {

    private val mainComponent: MainComponent
    private val mainConfig: MainConfig

    private val mGameContextRepository: GameContextRepository

    private val assetLoader: AssetLoader

    init {
        mainComponent = DaggerMainComponent.builder()
            .caveGame(this)
            .preferencesStore(preferencesStore)
            .build()

        mainConfig = mainComponent.mainConfig
        assetLoader = mainComponent.assetLoader
        mGameContextRepository = mainComponent.gameContextRepository
    }

    private fun initMainConfig() {
        val width = DEFAULT_VIEWPORT_WIDTH
        val height = width / Gdx.graphics.ratio

        mainConfig.mainComponent = mainComponent

        mGameContextRepository.apply {
            setGameDirectory(gameDataDirectoryPath)
            setTouch(isTouchScreen)
            setWidth(width)
            setHeight(height)
            setShowInfo(isDebug)
        }

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

    override fun quitGame() {
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