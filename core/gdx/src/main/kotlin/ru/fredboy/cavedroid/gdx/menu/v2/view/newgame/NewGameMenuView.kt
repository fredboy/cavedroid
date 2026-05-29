package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textButton
import ktx.scene2d.textField
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.newGameMenuView(viewModel: NewGameMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        when (state) {
            is NewGameMenuState.Show -> rebuild(viewModel, state.isKeyboardUp)
        }
    }
}

private fun Stage.rebuild(viewModel: NewGameMenuViewModel, isKeyboardUp: Boolean) {
    clear()
    val field = if (isKeyboardUp) {
        buildKeyboardUpLayout(viewModel)
    } else {
        buildDefaultLayout(viewModel)
    }
    if (isKeyboardUp) {
        keyboardFocus = field
    }
}

private fun attachFieldListeners(field: TextField, viewModel: NewGameMenuViewModel) {
    // Drive the IME ourselves from user gestures + the observer; scene2d's
    // focus-driven show/hide would race with the rebuild and re-fire the
    // observer, causing layout flicker.
    field.setOnscreenKeyboard { }

    field.addListener(
        object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                viewModel.onWorldNameChanged(field.text, field.cursorPosition)
            }
        },
    )

    field.onClick {
        if (viewModel.inlineTextInput.isSupported) {
            viewModel.inlineTextInput.trigger(
                initialText = field.text,
                initialCursor = field.cursorPosition,
                buttonText = viewModel.getLocalizedString("done"),
            ) { newText, newCursor ->
                viewModel.worldName = newText
                field.text = newText
                field.cursorPosition = newCursor
            }
        } else {
            Gdx.input.setOnscreenKeyboardVisible(true)
        }
    }
}

@Scene2dDsl
private fun Stage.buildDefaultLayout(viewModel: NewGameMenuViewModel): TextField {
    lateinit var worldNameField: TextField
    actors {
        menuButtonsTable(
            withGameLogo = false,
            withVersion = false,
        ) {
            worldNameField = textField(viewModel.worldName).apply {
                cursorPosition = viewModel.cursorPosition.coerceIn(0, text.length)
            }
            attachFieldListeners(worldNameField, viewModel)

            row()

            textButton(viewModel.getLocalizedString("creative")) {
                onClickWithSound(viewModel) {
                    viewModel.onCreativeClick(worldNameField.text)
                }
            }

            row()

            textButton(viewModel.getLocalizedString("survival")) {
                onClickWithSound(viewModel) {
                    viewModel.onSurvivalClick(worldNameField.text)
                }
            }

            row()

            textButton(viewModel.getLocalizedString("back")) {
                onClickWithSound(viewModel) {
                    viewModel.onBackClick()
                }
            }

            row()
        }
    }
    return worldNameField
}

@Scene2dDsl
private fun Stage.buildKeyboardUpLayout(viewModel: NewGameMenuViewModel): TextField {
    lateinit var worldNameField: TextField
    actors {
        table {
            setFillParent(true)
            background(
                TiledDrawable(
                    TextureRegionDrawable(
                        Scene2DSkin.defaultSkin.getRegion("background"),
                    ),
                ),
            )
            pad(8f)

            table {
                defaults()
                    .width(600f)
                    .height(60f)
                    .pad(10f)

                worldNameField = textField(viewModel.worldName).apply {
                    cursorPosition = viewModel.cursorPosition.coerceIn(0, text.length)
                }
                attachFieldListeners(worldNameField, viewModel)
            }.cell(expandY = true, align = Align.top)
        }
    }
    // Tap outside the field dismisses the IME; the observer then fires and
    // collapses the layout back to the default state.
    root.addListener(
        object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                var actor: Actor? = event.target
                while (actor != null) {
                    if (actor === worldNameField) return false
                    actor = actor.parent
                }
                Gdx.input.setOnscreenKeyboardVisible(false)
                return false
            }
        },
    )
    return worldNameField
}
