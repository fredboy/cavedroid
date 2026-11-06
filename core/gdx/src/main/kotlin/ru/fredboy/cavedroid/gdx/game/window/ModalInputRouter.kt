package ru.fredboy.cavedroid.gdx.game.window

import com.badlogic.gdx.InputProcessor

/** A modal consumes every event, including events that no actor handles. */
class ModalInputRouter(
    private val isModalOpen: () -> Boolean,
    private val modal: InputProcessor,
    private val gameplay: InputProcessor,
    private val synchronize: () -> Unit = {},
) : InputProcessor {
    private inline fun route(event: (InputProcessor) -> Boolean): Boolean {
        synchronize()
        val modalOpen = isModalOpen()
        return try {
            val handled = event(if (modalOpen) modal else gameplay)
            modalOpen || handled
        } finally {
            synchronize()
        }
    }

    override fun keyDown(keycode: Int) = route { it.keyDown(keycode) }
    override fun keyUp(keycode: Int) = route { it.keyUp(keycode) }
    override fun keyTyped(character: Char) = route { it.keyTyped(character) }
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = route { it.touchDown(screenX, screenY, pointer, button) }
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = route { it.touchUp(screenX, screenY, pointer, button) }
    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int) = route { it.touchCancelled(screenX, screenY, pointer, button) }
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = route { it.touchDragged(screenX, screenY, pointer) }
    override fun mouseMoved(screenX: Int, screenY: Int) = route { it.mouseMoved(screenX, screenY) }
    override fun scrolled(amountX: Float, amountY: Float) = route { it.scrolled(amountX, amountY) }
}
