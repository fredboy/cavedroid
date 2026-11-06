package ru.fredboy.cavedroid.gdx.game.window

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.game.window.GameWindowModel
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.GameInputProcessor
import javax.inject.Inject

/** One Stage per game session. Window types register factories rather than modifying this host. */
@GameScope
class GameWindowHost internal constructor(
    private val windows: GameWindowsManager,
    private val application: ApplicationContextRepository,
    private val gameplay: GameInputProcessor,
    private val factories: Map<String, @JvmSuppressWildcards GameWindowFactory>,
    private val stage: Stage,
) : Disposable {
    @Inject
    constructor(
        windows: GameWindowsManager,
        application: ApplicationContextRepository,
        gameplay: GameInputProcessor,
        factories: Map<String, @JvmSuppressWildcards GameWindowFactory>,
    ) : this(windows, application, gameplay, factories, Stage(ExtendViewport(640f, 320f)))

    private var model: GameWindowModel? = null
    private var view: GameWindowView? = null
    private var touch = application.isTouch()
    private var disposed = false
    private val modal = object : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                if (view?.onBack() != true) windows.closeWindow()
                return true
            }
            return stage.keyDown(keycode)
        }

        override fun keyUp(keycode: Int) = stage.keyUp(keycode)
        override fun keyTyped(character: Char) = stage.keyTyped(character)
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = stage.touchDown(screenX, screenY, pointer, button)
        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = stage.touchUp(screenX, screenY, pointer, button)
        override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int) = stage.touchCancelled(screenX, screenY, pointer, button)
        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = stage.touchDragged(screenX, screenY, pointer)
        override fun mouseMoved(screenX: Int, screenY: Int) = stage.mouseMoved(screenX, screenY)
        override fun scrolled(amountX: Float, amountY: Float) = stage.scrolled(amountX, amountY)
    }

    val inputProcessor = ModalInputRouter({ windows.currentSceneWindow != null }, modal, gameplay, ::synchronize)

    init {
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    private fun synchronize() {
        if (disposed) return
        val next = windows.currentSceneWindow
        val nextTouch = application.isTouch()
        if (model === next && touch == nextTouch) return
        releaseView()
        gameplay.suspendForWindow()
        model = next
        touch = nextTouch
        if (next != null) {
            val factory = requireNotNull(factories[next.key]) { "No Scene2D window factory registered for '${next.key}'" }
            view = factory.create(next, touch, windows::closeWindow).also { created ->
                stage.addActor(created.root)
                created.onShow(stage)
            }
        }
    }

    fun render(delta: Float) {
        synchronize()
        val active = view ?: return
        active.update(delta)
        stage.act(delta)
        stage.viewport.apply()
        stage.draw()
    }

    fun resize(width: Int, height: Int) {
        if (!disposed) stage.viewport.update(width, height, true)
    }

    private fun releaseView() {
        stage.cancelTouchFocus()
        stage.unfocusAll()
        Gdx.input.setOnscreenKeyboardVisible(false)
        view?.dispose()
        view = null
        stage.clear()
    }

    fun hide() {
        if (disposed) return
        if (windows.currentSceneWindow != null) windows.closeWindow()
        synchronize()
    }

    override fun dispose() {
        if (disposed) return
        hide()
        releaseView()
        stage.dispose()
        disposed = true
    }
}
