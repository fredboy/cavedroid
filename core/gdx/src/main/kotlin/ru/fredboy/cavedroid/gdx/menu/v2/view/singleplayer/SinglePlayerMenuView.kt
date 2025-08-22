package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.KTableWidget
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.imageButton
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton

@Scene2dDsl
suspend fun Stage.singlePlayerMenuView(viewModel: SinglePlayerMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
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

                scrollPane {
                    table {
                        state.saves.takeIf { it.isNotEmpty() }?.onEach { save ->
                            saveItem(
                                saveInfo = save,
                                onLoad = { viewModel.onLoadClick(save) },
                                onDelete = { viewModel.onDeleteClick(save) },
                            ).cell(
                                expandX = true,
                                fillX = true,
                                height = 200f,
                                pad = 32f,
                            )

                            row()
                        } ?: label("No worlds here yet... Try creating a new one")
                    }
                }.also { pane ->
                    setScrollFocus(pane)
                }.cell(
                    expand = true,
                    fill = true,
                    align = Align.center,
                )

                row()
                    .bottom()

                table {
                    textButton("New") {
                        onClick { viewModel.onNewGameClick() }
                    }.cell(
                        width = 400f,
                        height = 60f,
                        padRight = 16f,
                    )

                    textButton("Back") {
                        onClick { viewModel.onBackClick() }
                    }.cell(
                        width = 400f,
                        height = 60f,
                        padLeft = 16f,
                    )
                }.cell(
                    expandX = true,
                    fillX = true,
                    padTop = 16f,
                )
            }
        }
    }
}

@Scene2dDsl
private fun <S> KWidget<S>.saveItem(
    saveInfo: SaveInfoVo,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
): KTableWidget = table {
    background("shade_tile")
    pad(16f)

    imageButton {
        touchable = Touchable.disabled
        isDisabled = true

        saveInfo.screenshot?.let { screenshot ->
            image(screenshot) {
                touchable = Touchable.disabled
            }.cell(
                width = 80f,
                height = 80f,
            )
        }
    }.cell(
        width = 100f,
        height = 100f,
        align = Align.left,
    )

    table {
        label("${saveInfo.name} - ${saveInfo.gameMode.name}") {
            touchable = Touchable.disabled
        }.cell(
            expandX = true,
            fillX = true,
            pad = 16f,
        )

        row()

        label(saveInfo.timeCreated) {
            touchable = Touchable.disabled
        }.cell(
            expandX = true,
            fillX = true,
            pad = 16f,
        )
    }.cell(
        expandX = true,
        fillX = true,
    )

    table {
        defaults()
            .pad(16f)
            .width(200f)
            .height(60f)

        val loadButton = textButton("Load") {
            onClick {
                onLoad()
                isDisabled = true
            }
        }

        row()

        textButton("Delete") {
            onClick {
                onDelete()
                loadButton.isDisabled = true
                loadButton.touchable = Touchable.disabled

                touchable = Touchable.disabled
                isDisabled = true
            }
        }
    }.right()
}
