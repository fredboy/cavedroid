package ru.fredboy.cavedroid.gdx.game

import co.touchlab.kermit.Logger
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.common.model.WorldType
import ru.fredboy.cavedroid.data.configuration.model.GameContext
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.gdx.base.BaseScreen
import ru.fredboy.cavedroid.gdx.game.di.DaggerGameComponent
import ru.fredboy.cavedroid.gdx.game.di.GameComponent
import ru.fredboy.cavedroid.gdx.utils.applicationComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameScreen @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobParamsRepository: MobParamsRepository,
) : BaseScreen(applicationContextRepository) {

    var awaitingFirstFrame = false
        private set
    private var shown = false

    override val scaleFactor: Float
        get() = 0.5f

    var gameComponent: GameComponent? = null
        private set

    private fun getGameContext(gameConfig: StartGameConfig): GameContext = GameContext(
        isLoadGame = gameConfig is StartGameConfig.Load,
        saveGameDirectory = gameConfig.saveDirectory,
        worldName = gameConfig.worldName,
        requestedWorldWidth = (gameConfig as? StartGameConfig.New)?.worldSize?.blocks,
        requestedSeed = (gameConfig as? StartGameConfig.New)?.seed,
        worldType = (gameConfig as? StartGameConfig.New)?.worldType ?: WorldType.DEFAULT,
        showInfo = applicationContextRepository.preferShowDebug,
        showMap = false,
        joystick = Joystick(requireNotNull(mobParamsRepository.getMobParamsByKey("char")).speed),
        cameraContext = CameraContext(
            viewport = Rectangle(
                /* x = */ 0f,
                /* y = */ 0f,
                /* width = */ applicationContextRepository.getWidth(),
                /* height = */ applicationContextRepository.getHeight(),
            ),
            visibleWorld = Rectangle(
                /* x = */ 0f,
                /* y = */ 0f,
                /* width = */ applicationContextRepository.getWidth(),
                /* height = */ applicationContextRepository.getHeight(),
            ),
        ),
    )

    private fun getGameComponent(gameConfig: StartGameConfig): GameComponent {
        val gameContext = getGameContext(gameConfig)

        return DaggerGameComponent.builder()
            .applicationComponent(Gdx.app.applicationListener.applicationComponent)
            .gameContext(gameContext)
            .build()
    }

    private fun resetGameComponent() {
        if (gameComponent == null) return
        gameComponent?.gameSaveHelper?.dispose()
        try {
            gameComponent?.gameWindowHost?.dispose()
            gameComponent?.deathScreen?.dispose()
            gameComponent?.gameProc?.dispose()
        } catch (e: Exception) {
            logger.w(e) { "Recover faulty gameComponent" }
        } finally {
            gameComponent = null
        }
    }

    fun respawnPlayer() {
        gameComponent?.gameProc?.respawnPlayer()
    }

    fun newGame(gameConfig: StartGameConfig.New) {
        resetGameComponent()
        awaitingFirstFrame = true
        gameComponent = getGameComponent(gameConfig).apply {
            chunkEntityStreamer.initialize()
            gameProc.setPlayerGameMode(gameConfig.gameMode)
            gameRenderer.render(0f)
            gameSaveHelper.saveGame(false)
        }
    }

    fun loadGame(gameConfig: StartGameConfig.Load) {
        resetGameComponent()
        awaitingFirstFrame = true
        gameComponent = getGameComponent(gameConfig).apply {
            chunkEntityStreamer.initialize()
        }
    }

    fun saveGame() {
        val gameComponent = requireNotNull(gameComponent) {
            "GameScreen#saveGame: gameComponent was not set before saveGame()"
        }

        gameComponent.gameRenderer.render(0f)
        gameComponent.gameSaveHelper.saveGame(overwrite = true)
    }

    override fun show() {
        shown = true
        val gameComponent = requireNotNull(gameComponent) {
            "GameScreen#show: gameComponent was not set before show"
        }
        gameComponent.gameProc.show()
        Gdx.input.inputProcessor = gameComponent.gameWindowHost.inputProcessor
        gameComponent.gameRenderer.render(0f)
    }

    override fun render(delta: Float) {
        val proc = requireNotNull(gameComponent?.gameProc) {
            "GameScreen#render: gameComponent was not set before render"
        }
        val firstFrame = awaitingFirstFrame
        try {
            // Preparing the first view can span a long frame; it is not elapsed simulation time.
            proc.update(if (firstFrame) 0f else delta)
            gameComponent?.gameWindowHost?.render(if (firstFrame) 0f else delta)
            if (!firstFrame && shown && gameComponent?.gameRenderer?.isReady == true) {
                gameComponent?.gameSaveHelper?.updateAutoSave(delta)
            }
        } finally {
            awaitingFirstFrame = false
        }
    }

    override fun onResize(width: Int, height: Int) {
        gameComponent?.gameContextRepository?.getCameraContext()?.let { cameraContext ->
            cameraContext.viewport.apply {
                setWidth(applicationContextRepository.getWidth())
                setHeight(applicationContextRepository.getHeight())
            }

            cameraContext.visibleWorld.apply {
                setWidth(applicationContextRepository.getWidth())
                setHeight(applicationContextRepository.getHeight())
            }
        }

        gameComponent?.gameProc?.onResize()
        gameComponent?.gameWindowHost?.resize(width, height)
    }

    override fun pause() {
        gameComponent?.gameSaveHelper?.drain()
        gameComponent?.gameProc?.onGamePaused()
        gameComponent?.gameWindowHost?.hide()
    }

    override fun resume() {
        gameComponent?.gameProc?.onGameResumed()
    }

    override fun hide() {
        gameComponent?.gameSaveHelper?.drain()
        gameComponent?.gameWindowHost?.hide()
        gameComponent?.gameProc?.hide()
        shown = false
    }

    override fun onDispose() {
        resetGameComponent()
    }

    companion object {
        private val logger = Logger.withTag("GameScreen")
    }
}
