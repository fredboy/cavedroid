package ru.fredboy.cavedroid.gdx.menu.v2.view.editworld

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.imageButton
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton
import ktx.scene2d.textField
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.editWorldMenuView(viewModel: EditWorldMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        clear()
        when (state) {
            is EditWorldMenuState.Loading -> messageScreen(viewModel.getLocalizedString("loading"))
            is EditWorldMenuState.Working -> messageScreen(state.message)
            is EditWorldMenuState.ShowInfo -> showInfo(viewModel, state)
            is EditWorldMenuState.ConfirmDelete -> confirmDelete(viewModel, state)
            is EditWorldMenuState.Renaming -> renaming(viewModel, state)
        }
    }
}

@Scene2dDsl
private fun Stage.backgroundTable(content: KTableWidget.() -> Unit) {
    actors {
        table {
            setFillParent(true)
            background(
                TiledDrawable(
                    TextureRegionDrawable(
                        skin.getRegion("background"),
                    ),
                ),
            )
            pad(8f)

            content()
        }
    }
}

@Scene2dDsl
private fun Stage.messageScreen(message: String) {
    backgroundTable {
        label(message)
    }
}

@Scene2dDsl
private fun Stage.showInfo(viewModel: EditWorldMenuViewModel, state: EditWorldMenuState.ShowInfo) {
    val details = state.details

    backgroundTable {
        table {
            imageButton {
                touchable = Touchable.disabled
                isDisabled = true

                details.screenshot?.let { screenshot ->
                    image(screenshot) {
                        touchable = Touchable.disabled
                    }.cell(width = 80f, height = 80f)
                }
            }.cell(width = 100f, height = 100f, align = Align.left)

            table {
                infoRow(viewModel.getLocalizedString("name"), details.name)
                infoRow(
                    viewModel.getLocalizedString("gameMode"),
                    viewModel.getLocalizedString(details.gameMode.name.lowercase()),
                )
                infoRow(viewModel.getLocalizedString("mapSize"), details.mapSize)
                infoRow(viewModel.getLocalizedString("diskSize"), details.diskSize)
                infoRow(viewModel.getLocalizedString("seed"), details.seed ?: "—")
                infoRow(viewModel.getLocalizedString("createdOn"), details.created ?: "—")
                infoRow(viewModel.getLocalizedString("lastModified"), details.lastModified)
            }.cell(expandX = true, fillX = true, padLeft = 16f, align = Align.left)
        }.cell(expandX = true, fillX = true)

        row()

        state.statusMessage?.let { message ->
            label(message) {
                setAlignment(Align.center)
            }.cell(expandX = true, fillX = true, padTop = 8f)
            row()
        }

        table {
            defaults().width(400f).height(60f).pad(2f)

            textButton(viewModel.getLocalizedString("rename")) {
                onClickWithSound(viewModel) { viewModel.onRenameClick() }
            }

            if (viewModel.isExportSupported) {
                textButton(viewModel.getLocalizedString("exportSave")) {
                    onClickWithSound(viewModel) { viewModel.onExportClick() }
                }

                row()
            }

            textButton(viewModel.getLocalizedString("delete")) {
                onClickWithSound(viewModel) { viewModel.onDeleteClick() }
            }

            if (!viewModel.isExportSupported) {
                row()
            }

            textButton(viewModel.getLocalizedString("back")) {
                onClickWithSound(viewModel) { viewModel.onBackClick() }
            }.cell(
                width = 816f.takeIf { !viewModel.isExportSupported },
                colspan = 2.takeIf { !viewModel.isExportSupported },
            )
        }.cell(expandX = true, fillX = true, padTop = 16f)
    }
}

@Scene2dDsl
private fun KTableWidget.infoRow(title: String, value: String) {
    label("$title: ") {
        setAlignment(Align.left)
    }.cell(align = Align.left, padRight = 8f)

    label(value) {
        setEllipsis(true)
        setAlignment(Align.left)
    }.cell(expandX = true, fillX = true, minWidth = 50f, maxWidth = 400f, align = Align.left)

    row()
}

@Scene2dDsl
private fun Stage.confirmDelete(viewModel: EditWorldMenuViewModel, state: EditWorldMenuState.ConfirmDelete) {
    backgroundTable {
        label(viewModel.getFormattedString("deleteWorldWithName", state.worldName)) {
            setAlignment(Align.center)
        }.cell(align = Align.center)

        row()

        table {
            textButton(viewModel.getLocalizedString("confirm")) {
                onClickWithSound(viewModel) { viewModel.onConfirmDelete() }
            }.cell(width = 400f, height = 60f, padRight = 16f)

            textButton(viewModel.getLocalizedString("cancel")) {
                onClickWithSound(viewModel) { viewModel.onCancelDelete() }
            }.cell(width = 400f, height = 60f, padLeft = 16f)
        }.cell(expandX = true, fillX = true, padTop = 16f)
    }
}

@Scene2dDsl
private fun Stage.renaming(viewModel: EditWorldMenuViewModel, state: EditWorldMenuState.Renaming) {
    val field = if (state.isKeyboardUp) {
        buildRenameKeyboardUpLayout(viewModel)
    } else {
        buildRenameLayout(viewModel)
    }
    if (state.isKeyboardUp) {
        keyboardFocus = field
    }
}

private fun attachRenameListeners(field: TextField, viewModel: EditWorldMenuViewModel) {
    // Drive the IME from gestures + the observer, matching NewGameMenuView.
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
                viewModel.renameText = newText
                field.text = newText
                field.cursorPosition = newCursor
            }
        } else {
            Gdx.input.setOnscreenKeyboardVisible(true)
        }
    }
}

@Scene2dDsl
private fun Stage.buildRenameLayout(viewModel: EditWorldMenuViewModel): TextField {
    lateinit var nameField: TextField
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
                defaults().width(600f).height(60f).pad(10f)

                nameField = textField(viewModel.renameText).apply {
                    cursorPosition = viewModel.cursorPosition.coerceIn(0, text.length)
                }
                attachRenameListeners(nameField, viewModel)

                row()

                textButton(viewModel.getLocalizedString("confirm")) {
                    onClickWithSound(viewModel) {
                        viewModel.onConfirmRename(nameField.text)
                    }
                }

                row()

                textButton(viewModel.getLocalizedString("cancel")) {
                    onClickWithSound(viewModel) {
                        viewModel.onCancelRename()
                    }
                }
            }
        }
    }
    return nameField
}

@Scene2dDsl
private fun Stage.buildRenameKeyboardUpLayout(viewModel: EditWorldMenuViewModel): TextField {
    lateinit var nameField: TextField
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
                defaults().width(600f).height(60f).pad(10f)

                nameField = textField(viewModel.renameText).apply {
                    cursorPosition = viewModel.cursorPosition.coerceIn(0, text.length)
                }
                attachRenameListeners(nameField, viewModel)
            }.cell(expandY = true, align = Align.top)
        }
    }
    // Tap outside the field dismisses the IME; the observer collapses the layout.
    root.addListener(
        object : InputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                var actor: Actor? = event.target
                while (actor != null) {
                    if (actor === nameField) return false
                    actor = actor.parent
                }
                Gdx.input.setOnscreenKeyboardVisible(false)
                return false
            }
        },
    )
    return nameField
}
