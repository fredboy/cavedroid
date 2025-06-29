package ru.deadsoftware.cavedroid

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.common.utils.ratio
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
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

    init {
        mainComponent = DaggerMainComponent.builder()
            .caveGame(this)
            .preferencesStore(preferencesStore)
            .build()

        mainConfig = mainComponent.mainConfig
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

            setCameraContext(
                CameraContext(
                    viewport = Rectangle(0f, 0f, width, height),
                    camera = OrthographicCamera().apply {
                        setToOrtho(true, width, height)
                    }
                )
            )
        }

        Gdx.app.logLevel = if (isDebug) Application.LOG_DEBUG else Application.LOG_ERROR
    }

    fun newGame(gameMode: Int) {
        setScreen(mainComponent.gameScreen.apply { newGame(gameMode) })
    }

    override fun newGameCreative() {
        newGame(1)
    }

    override fun newGameSurvival() {
        newGame(0)
    }

    override fun loadGame() {
        setScreen(mainComponent.gameScreen.apply { loadGame() })
    }

    override fun quitGame() {
        screen?.dispose()
        setScreen(mainComponent.menuScreen.apply { resetMenu() })
    }

    override fun exitGame() {
        Gdx.app.exit()
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
    }

}