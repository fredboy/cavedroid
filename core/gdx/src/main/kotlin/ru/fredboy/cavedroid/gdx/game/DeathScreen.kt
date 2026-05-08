package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.base.BaseScreen
import ru.fredboy.cavedroid.gdx.menu.di.DaggerMenuComponent
import ru.fredboy.cavedroid.gdx.menu.v2.MenuNavigationController
import ru.fredboy.cavedroid.gdx.menu.v2.view.death.DeathScreenNavKey
import ru.fredboy.cavedroid.gdx.utils.applicationComponent
import javax.inject.Inject

@GameScope
class DeathScreen @Inject constructor(
    private val gameProc: GameProc,
    private val applicationContextRepository: ApplicationContextRepository,
) : BaseScreen(applicationContextRepository) {

    private val menuNavigationController: MenuNavigationController =
        DaggerMenuComponent.builder()
            .applicationComponent(Gdx.app.applicationListener.applicationComponent)
            .rootNavKey(DeathScreenNavKey)
            .build()
            .menuNavigationController

    private val currentStage get() = menuNavigationController.navRootStage

    override fun show() {
        Gdx.input.inputProcessor = currentStage
        menuNavigationController.reset()
        currentStage.draw()
    }

    override fun render(delta: Float) {
        gameProc.update(delta)
        currentStage.act(delta)
        currentStage.draw()
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
