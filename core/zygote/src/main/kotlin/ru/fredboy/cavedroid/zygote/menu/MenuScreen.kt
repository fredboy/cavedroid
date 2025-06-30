package ru.fredboy.cavedroid.zygote.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository
import ru.fredboy.cavedroid.zygote.CaveDroidApplication
import ru.fredboy.cavedroid.zygote.menu.di.DaggerMenuComponent
import ru.fredboy.cavedroid.zygote.menu.di.MenuComponent
import ru.fredboy.cavedroid.zygote.menu.input.MenuInputProcessor
import ru.fredboy.cavedroid.zygote.menu.renderer.MenuRenderer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuScreen @Inject constructor() : Screen {

    private val menuRenderer: MenuRenderer

    private val menuInputProcessor: MenuInputProcessor

    private val menuButtonRepository: MenuButtonRepository

    init {
        val menuComponent: MenuComponent = DaggerMenuComponent.builder()
            .applicationComponent((Gdx.app.applicationListener as CaveDroidApplication).applicationComponent)
            .build()

        menuRenderer = menuComponent.menuRenderer
        menuInputProcessor = menuComponent.menuInputProcessor
        menuButtonRepository = menuComponent.menuButtonRepository
    }

    fun resetMenu() {
        menuButtonRepository.setCurrentMenu("main")
    }

    override fun show() {
        Gdx.input.inputProcessor = menuInputProcessor
    }

    override fun render(delta: Float) {
        menuRenderer.render(delta)
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
        menuRenderer.dispose()
        menuButtonRepository.dispose()
    }
}