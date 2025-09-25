package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.common.model.StartGameConfig
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

    override val scaleFactor: Float
        get() = 0.5f

    var gameComponent: GameComponent? = null
        private set

    private fun getGameContext(gameConfig: StartGameConfig): GameContext = GameContext(
        isLoadGame = gameConfig is StartGameConfig.Load,
        saveGameDirectory = gameConfig.saveDirectory,
        worldName = gameConfig.worldName,
        showInfo = false,
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
        gameComponent?.gameProc?.dispose()
        gameComponent = null
    }

    fun newGame(gameConfig: StartGameConfig.New) {
        resetGameComponent()
        gameComponent = getGameComponent(gameConfig).apply {
            gameProc.setPlayerGameMode(gameConfig.gameMode)
            gameRenderer.render(0f)
            gameSaveHelper.saveGame(false)
        }
    }

    fun loadGame(gameConfig: StartGameConfig.Load) {
        resetGameComponent()
        gameComponent = getGameComponent(gameConfig)
    }

    fun saveGame() {
        val gameComponent = requireNotNull(gameComponent) {
            "GameScreen#saveGame: gameComponent was not set before saveGame()"
        }

        gameComponent.gameRenderer.render(0f)
        gameComponent.gameSaveHelper.saveGame(overwrite = true)
    }

    override fun show() {
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
        proc.update(delta)
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
    }

    override fun pause() {
        gameComponent?.gameProc?.onGamePaused()
    }

    override fun resume() {
        gameComponent?.gameProc?.onGameResumed()
    }

    override fun hide() {
    }

    override fun onDispose() {
        resetGameComponent()
    }
}
