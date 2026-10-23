package ru.fredboy.cavedroid.gdx.game

import co.touchlab.kermit.Logger
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.common.model.WorldType
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceResult
import ru.fredboy.cavedroid.common.performance.PerformanceSpan
import ru.fredboy.cavedroid.common.performance.PerformanceTrigger
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
    private val performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobParamsRepository: MobParamsRepository,
) : BaseScreen(applicationContextRepository) {

    var awaitingFirstFrame = false
        private set
    private var shown = false
    private var opening: PerformanceSpan? = null

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
        performanceMonitor.setGameplay(false)
        opening?.finish(PerformanceResult.CANCELLED)
        opening = null
        performanceMonitor.beginReleaseSession()
        val release = performanceMonitor.beginOperation(PerformanceOperation.WORLD_RELEASE)
        try {
            gameComponent?.deathScreen?.dispose()
            gameComponent?.gameProc?.dispose()
        } catch (e: Exception) {
            release?.fail(e)
            logger.w(e) { "Recover faulty gameComponent" }
        } finally {
            release?.finish()
            performanceMonitor.releaseSession(release?.result ?: PerformanceResult.SUCCESS)
            gameComponent = null
        }
    }

    fun failOpening(error: Throwable) {
        opening?.fail(error)
        opening?.finish()
        opening = null
        awaitingFirstFrame = false
    }

    fun respawnPlayer() {
        gameComponent?.gameProc?.respawnPlayer()
    }

    fun newGame(gameConfig: StartGameConfig.New) {
        val creation = performanceMonitor.beginOperation(PerformanceOperation.WORLD_CREATE)
        resetGameComponent()
        awaitingFirstFrame = true
        performanceMonitor.beginSession()
        opening = creation
        try {
            gameComponent = getGameComponent(gameConfig).apply {
                chunkEntityStreamer.initialize()
                gameProc.setPlayerGameMode(gameConfig.gameMode)
                val initialSave = performanceMonitor.beginOperation(PerformanceOperation.WORLD_SAVE, PerformanceTrigger.INITIAL)
                try {
                    gameRenderer.render(0f)
                    gameSaveHelper.saveGame(false)
                } catch (error: Throwable) {
                    initialSave?.fail(error)
                    throw error
                } finally {
                    initialSave?.finish()
                }
            }
        } catch (error: Throwable) {
            opening?.fail(error)
            opening?.finish()
            opening = null
            throw error
        }
    }

    fun loadGame(gameConfig: StartGameConfig.Load) {
        val loading = performanceMonitor.beginOperation(PerformanceOperation.WORLD_LOAD)
        resetGameComponent()
        awaitingFirstFrame = true
        performanceMonitor.beginSession()
        opening = loading
        try {
            gameComponent = getGameComponent(gameConfig).apply {
                chunkEntityStreamer.initialize()
            }
        } catch (error: Throwable) {
            opening?.fail(error)
            opening?.finish()
            opening = null
            throw error
        }
    }

    fun saveGame() {
        val gameComponent = requireNotNull(gameComponent) {
            "GameScreen#saveGame: gameComponent was not set before saveGame()"
        }

        performanceMonitor.setGameplay(false)
        val save = performanceMonitor.beginOperation(PerformanceOperation.WORLD_SAVE)
        try {
            gameComponent.gameRenderer.render(0f)
            gameComponent.gameSaveHelper.saveGame(overwrite = true)
        } catch (error: Throwable) {
            save?.fail(error)
            throw error
        } finally {
            save?.finish()
            performanceMonitor.setGameplay(shown && !awaitingFirstFrame)
        }
    }

    override fun show() {
        shown = true
        updatePerformanceContext()
        val gameComponent = requireNotNull(gameComponent) {
            "GameScreen#show: gameComponent was not set before show"
        }
        gameComponent.gameProc.show()
        gameComponent.gameRenderer.render(0f)
    }

    override fun render(delta: Float) {
        val proc = requireNotNull(gameComponent?.gameProc) {
            "GameScreen#render: gameComponent was not set before render"
        }
        val firstFrame = awaitingFirstFrame
        if (firstFrame) performanceMonitor.setGameplay(false)
        performanceMonitor.beginFrame()
        try {
            proc.update(delta)
        } catch (error: Throwable) {
            opening?.fail(error)
            throw error
        } finally {
            performanceMonitor.endFrame()
            opening?.finish()
            opening = null
            awaitingFirstFrame = false
            if (firstFrame) performanceMonitor.setGameplay(shown)
        }
    }

    private fun updatePerformanceContext() {
        if (!performanceMonitor.available) return
        performanceMonitor.updateViewContext(
            Gdx.graphics.width,
            Gdx.graphics.height,
            applicationContextRepository.getWidth().toInt(),
            applicationContextRepository.getHeight().toInt(),
            applicationContextRepository.getLightingBackend().name.lowercase(),
            gameComponent?.gameContextRepository?.shouldShowInfo() ?: applicationContextRepository.preferShowDebug,
        )
    }

    override fun onResize(width: Int, height: Int) {
        updatePerformanceContext()
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
    }

    override fun pause() {
        gameComponent?.gameProc?.onGamePaused()
    }

    override fun resume() {
        gameComponent?.gameProc?.onGameResumed()
    }

    override fun hide() {
        shown = false
    }

    override fun onDispose() {
        resetGameComponent()
    }

    companion object {
        private val logger = Logger.withTag("GameScreen")
    }
}
