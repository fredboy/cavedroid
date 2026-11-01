package ru.fredboy.cavedroid.common.mvvm

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.utils.Layout
import kotlinx.coroutines.flow.Flow
import ktx.scene2d.Scene2dDsl

/**
 * Replaces the actor tree on each state emission, retaining offsets of scroll panes with stable,
 * unique [Actor.name] values. Positions also survive temporary states without those panes.
 * The caller must cancel collection when the view leaves the stage.
 */
@Scene2dDsl
suspend fun <T> Stage.collectViewState(state: Flow<T>, render: (T) -> Unit) {
    val scrollState = ViewScrollState()
    state.collect { value ->
        scrollState.capture(root)
        val focusedPaneName = (scrollFocus as? ScrollPane)?.name
        clear()
        render(value)
        scrollState.restore(root)
        if (focusedPaneName != null) {
            root.findActor<ScrollPane>(focusedPaneName)?.let { scrollFocus = it }
        }
    }
}

internal class ViewScrollState {
    private data class Position(val x: Float, val y: Float)

    private val positions = mutableMapOf<String, Position>()

    fun capture(root: Actor) {
        visit(root) { actor ->
            if (actor is ScrollPane) {
                actor.name?.let { positions[it] = Position(actor.scrollX, actor.scrollY) }
            }
        }
    }

    fun restore(root: Actor) {
        visit(root) { actor ->
            // Parents must lay out first: setScrollX/Y clamp against the pane's calculated bounds.
            (actor as? Layout)?.validate()
            if (actor is ScrollPane) {
                positions[actor.name]?.let { position ->
                    actor.scrollX = position.x
                    actor.scrollY = position.y
                    actor.updateVisualScroll()
                }
            }
        }
    }

    private fun visit(actor: Actor, action: (Actor) -> Unit) {
        action(actor)
        if (actor is Group) {
            actor.children.forEach { visit(it, action) }
        }
    }
}
