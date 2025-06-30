package ru.fredboy.cavedroid.zygote.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.data.configuration.model.GameContext
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.zygote.CaveDroidApplication
import ru.fredboy.cavedroid.zygote.game.di.DaggerGameComponent
import ru.fredboy.cavedroid.zygote.game.di.GameComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameScreen @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : Screen {

    private var gameProc: GameProc? = null

    private fun getGameContext(isLoadGame: Boolean): GameContext {
        return GameContext(
            isLoadGame = isLoadGame,
            showInfo = false,
            showMap = false,
            isFullscreen = false,
            useDynamicCamera = false,
            joystick = Joystick(Player.SPEED),
            cameraContext = CameraContext(
                viewport = Rectangle(
                    /* x = */ 0f,
                    /* y = */ 0f,
                    /* width = */ applicationContextRepository.getWidth(),
                    /* height = */ applicationContextRepository.getHeight(),
                ),
                camera = OrthographicCamera().apply {
                    setToOrtho(
                        /* yDown = */ true,
                        /* viewportWidth = */ applicationContextRepository.getWidth(),
                        /* viewportHeight = */ applicationContextRepository.getHeight(),
                    )
                }
            )
        )
    }

    private fun getGameComponent(isLoadGame: Boolean): GameComponent {
        val gameContext = getGameContext(isLoadGame)

        return DaggerGameComponent.builder()
            .applicationComponent((Gdx.app.applicationListener as CaveDroidApplication).applicationComponent)
            .gameContext(gameContext)
            .build()
    }

    private fun resetGameProc() {
        gameProc?.dispose()
        gameProc = null
    }

    fun newGame(gameMode: Int) {
        resetGameProc()
        gameProc = getGameComponent(false).gameProc.apply {
            setPlayerGameMode(gameMode)
        }
    }

    fun loadGame() {
        resetGameProc()
        gameProc = getGameComponent(true).gameProc
    }

    override fun show() {
        val proc = requireNotNull(gameProc) {
            "GameScreen#show: gameProc was not set before show"
        }
        proc.show()
    }

    override fun render(delta: Float) {
        val proc = requireNotNull(gameProc) {
            "GameScreen#render: gameProc was not set before render"
        }
        proc.update(delta)
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {
        resetGameProc()
    }
}