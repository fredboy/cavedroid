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
import ru.fredboy.cavedroid.common.CaveDroidConstants.MAX_SAVES_COUNT

@Scene2dDsl
suspend fun Stage.singlePlayerMenuView(viewModel: SinglePlayerMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        when (state) {
            is SinglePlayerMenuState.LoadingWorld -> loading(viewModel)
            is SinglePlayerMenuState.ShowList -> savesList(viewModel, state)
        }
    }
}

@Scene2dDsl
private fun Stage.savesList(viewModel: SinglePlayerMenuViewModel, state: SinglePlayerMenuState.ShowList) {
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
                            viewModel = viewModel,
                            saveInfo = save,
                            onLoad = { viewModel.onLoadClick(save) },
                            onDelete = { viewModel.onDeleteClick(save) },
                        ).cell(
                            expandX = true,
                            fillX = true,
                            height = 200f,
                            padBottom = 32f,
                        )

                        row()
                    } ?: label(viewModel.getLocalizedString("noWorlds"))
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
                textButton(viewModel.getLocalizedString("newWorld")) {
                    onClick {
                        if (!isDisabled) {
                            viewModel.onNewGameClick()
                        }
                    }

                    isDisabled = state.saves.size >= MAX_SAVES_COUNT
                }.cell(
                    width = 400f,
                    height = 60f,
                    padRight = 16f,
                )

                textButton(viewModel.getLocalizedString("back")) {
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

@Scene2dDsl
private fun Stage.loading(viewModel: SinglePlayerMenuViewModel) {
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

            label(viewModel.getLocalizedString("loadingWorld"))
        }
    }
}

@Scene2dDsl
private fun <S> KWidget<S>.saveItem(
    viewModel: SinglePlayerMenuViewModel,
    saveInfo: SaveInfoVo,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
): KTableWidget = table {
    background("shade_tile")
    pad(8f)

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
        table {
            label(saveInfo.name) {
                setEllipsis(true)
            }.cell(
                growX = false,
                fillX = false,
                minWidth = 50f,
                maxWidth = 250f,
                align = Align.left,
            )

            label(" - ${saveInfo.gameMode.name}")
                .cell(
                    expandX = true,
                    fillX = true,
                    align = Align.left,
                )
        }.cell(
            expandX = true,
            fillX = true,
            padLeft = 16f,
            align = Align.left,
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
            .width(300f)
            .height(60f)

        val loadButton = textButton(viewModel.getLocalizedString("load")) {
            isDisabled = !saveInfo.isSupported

            onClick {
                if (isDisabled) {
                    return@onClick
                }

                onLoad()
                isDisabled = true
            }
        }

        row()

        textButton(viewModel.getLocalizedString("delete")) {
            onClick {
                if (isDisabled) {
                    return@onClick
                }

                onDelete()
                loadButton.isDisabled = true
                loadButton.touchable = Touchable.disabled

                touchable = Touchable.disabled
                isDisabled = true
            }
        }
    }.right()
}
