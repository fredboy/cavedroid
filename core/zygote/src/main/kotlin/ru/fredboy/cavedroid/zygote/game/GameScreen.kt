package ru.fredboy.cavedroid.zygote.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.data.configuration.model.GameContext
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.zygote.CaveDroidApplication
import ru.fredboy.cavedroid.zygote.base.BaseScreen
import ru.fredboy.cavedroid.zygote.game.di.DaggerGameComponent
import ru.fredboy.cavedroid.zygote.game.di.GameComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameScreen @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : BaseScreen(applicationContextRepository) {

    private var gameComponent: GameComponent? = null

    private fun getGameContext(isLoadGame: Boolean): GameContext = GameContext(
        isLoadGame = isLoadGame,
        showInfo = false,
        showMap = false,
        joystick = Joystick(Player.SPEED),
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

    private fun getGameComponent(isLoadGame: Boolean): GameComponent {
        val gameContext = getGameContext(isLoadGame)

        return DaggerGameComponent.builder()
            .applicationComponent((Gdx.app.applicationListener as CaveDroidApplication).applicationComponent)
            .gameContext(gameContext)
            .build()
    }

    private fun resetGameComponent() {
        gameComponent?.gameProc?.dispose()
        gameComponent = null
    }

    fun newGame(gameMode: Int) {
        resetGameComponent()
        gameComponent = getGameComponent(false).apply {
            gameProc.setPlayerGameMode(gameMode)
        }
    }

    fun loadGame() {
        resetGameComponent()
        gameComponent = getGameComponent(true)
    }

    override fun show() {
        val proc = requireNotNull(gameComponent?.gameProc) {
            "GameScreen#show: gameComponent was not set before show"
        }
        proc.show()
    }

    override fun render(delta: Float) {
        val proc = requireNotNull(gameComponent?.gameProc) {
            "GameScreen#render: gameComponent was not set before render"
        }
        proc.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        gameComponent?.gameContextRepository?.getCameraContext()?.let { cameraContext ->
            cameraContext.viewport.apply {
                setWidth(applicationContextRepository.getWidth())
                setHeight(applicationContextRepository.getHeight())
            }
        }

        gameComponent?.gameProc?.onResize()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
    }

    override fun dispose() {
        resetGameComponent()
    }
}
