package ru.fredboy.cavedroid.gdx.game.window

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.game.window.GameWindowModel

/** Implementations own actors/listeners, but never dispose the shared application skin. */
interface GameWindowView : Disposable {
    val root: Actor

    fun onShow(stage: Stage) = Unit

    fun update(delta: Float) = Unit

    /** True if a nested operation consumed Back (for example, cancelling a rename). */
    fun onBack(): Boolean = false
}

interface GameWindowFactory {
    fun create(model: GameWindowModel, touch: Boolean, close: () -> Unit): GameWindowView
}
