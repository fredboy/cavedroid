package ru.fredboy.cavedroid.gdx.game.window

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align

/** Shared frame and modal behavior; subclasses supply contents, not their own Stage. */
abstract class Scene2dGameWindow(
    protected val skin: Skin,
    protected val touch: Boolean,
    title: String,
    width: Float,
    private val designHeight: Float,
    close: () -> Unit,
) : GameWindowView {
    protected val window = Window(title, skin, if (touch) "touch" else "default").apply {
        isModal = true
        isMovable = false
        isResizable = false
        titleLabel.setEllipsis(true)
        titleTable.add(ImageButton(skin, if (touch) "close-touch" else "close").apply { onChange(close) })
            .size(if (touch) 44f else 20f).padLeft(8f)
        padTop(if (touch) 50f else 28f)
    }

    protected val content = Table().also { window.add(it).grow() }

    final override val root = Table().apply {
        setFillParent(true)
        add(window).width(width).height(designHeight)
    }

    protected fun compactForKeyboard(visible: Boolean, compactHeight: Float) {
        root.align(if (visible) Align.top else Align.center)
        root.getCell(window).height(if (visible) compactHeight else designHeight)
        root.invalidateHierarchy()
    }

    override fun onShow(stage: Stage) {
        stage.keyboardFocus = window
    }

    override fun dispose() = Unit
}

internal fun Actor.onChange(action: () -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent?, actor: Actor?) = action()
    })
}
