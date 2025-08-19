package ru.fredboy.cavedroid.gdx.menu.v2

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import ktx.scene2d.Scene2DSkin
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import ru.fredboy.cavedroid.gdx.base.BaseScreen
import ru.fredboy.cavedroid.gdx.menu.di.DaggerMenuComponent
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavRootStage
import ru.fredboy.cavedroid.gdx.menu.v2.stage.main.MainMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.stage.main.mainMenuView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuScreen @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : BaseScreen(applicationContextRepository) {

    private val menuNavigationController: MenuNavigationController

    private val currentStage get() = menuNavigationController.navRootStage

    init {
        val menuComponent = DaggerMenuComponent.builder()
            .applicationComponent((Gdx.app.applicationListener as CaveDroidApplication).applicationComponent)
            .build()

        menuNavigationController = menuComponent.menuNavigationController
    }

    override fun show() {
        currentStage.let { stage ->
            Gdx.input.inputProcessor = stage
        }
    }

    override fun render(delta: Float) {
        currentStage.let { stage ->
            stage.act(delta)
            stage.draw()
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        currentStage.viewport.update(width, height, true)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {}

    override fun dispose() {
        menuNavigationController.dispose()
    }
}
