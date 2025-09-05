package ru.fredboy.cavedroid.gdx.menu.v2

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import ru.fredboy.cavedroid.gdx.base.BaseScreen
import ru.fredboy.cavedroid.gdx.game.GameScreen
import ru.fredboy.cavedroid.gdx.menu.di.DaggerMenuComponent
import ru.fredboy.cavedroid.gdx.menu.v2.view.pause.PauseMenuNavKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PauseMenuScreen @Inject constructor(
    private val gameScreen: GameScreen,
    private val applicationContextRepository: ApplicationContextRepository,
) : BaseScreen(applicationContextRepository) {

    private val menuNavigationController: MenuNavigationController

    private val currentStage get() = menuNavigationController.navRootStage

    init {
        val menuComponent = DaggerMenuComponent.builder()
            .applicationComponent((Gdx.app.applicationListener as CaveDroidApplication).applicationComponent)
            .rootNavKey(PauseMenuNavKey)
            .build()

        menuNavigationController = menuComponent.menuNavigationController
    }

    override fun show() {
        currentStage.let { stage ->
            Gdx.input.inputProcessor = stage
            menuNavigationController.reset()
            currentStage.draw()
        }
    }

    override fun render(delta: Float) {
        gameScreen.gameComponent?.gameRenderer?.render(0f)
        currentStage.let { stage ->
            stage.act(delta)
            stage.draw()
        }
    }

    override fun onResize(width: Int, height: Int) {
        currentStage.viewport.setWorldSize(
            applicationContextRepository.getWidth(),
            applicationContextRepository.getHeight(),
        )
        currentStage.viewport.update(width, height, true)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {}

    override fun onDispose() {
        menuNavigationController.dispose()
    }
}
