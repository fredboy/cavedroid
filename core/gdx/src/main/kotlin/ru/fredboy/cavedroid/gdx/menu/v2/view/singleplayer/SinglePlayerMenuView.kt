package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
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
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.singlePlayerMenuView(viewModel: SinglePlayerMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        when (state) {
            is SinglePlayerMenuState.LoadingWorld -> loading(viewModel)
            is SinglePlayerMenuState.LoadingFailed -> loadingFailed(viewModel)
            is SinglePlayerMenuState.ShowList -> savesList(viewModel, state)
            is SinglePlayerMenuState.LoadingList -> loadingList(viewModel)
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
                            onEdit = { viewModel.onEditClick(save) },
                        ).cell(
                            expandX = true,
                            fillX = true,
                            height = 200f,
                            padBottom = 32f,
                        )

                        row()
                    } ?: ifTrue(state.showMessageIfEmpty) {
                        label(viewModel.getLocalizedString("noWorlds"))
                    }
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
                val buttonWidth = if (viewModel.isImportSupported) {
                    266f
                } else {
                    400f
                }

                textButton(viewModel.getLocalizedString("newWorld")) {
                    onClickWithSound(viewModel) {
                        if (!isDisabled) {
                            viewModel.onNewGameClick()
                        }
                    }

                    isDisabled = state.saves.size >= MAX_SAVES_COUNT
                }.cell(
                    width = buttonWidth,
                    height = 60f,
                    padRight = 16f,
                )

                if (viewModel.isImportSupported) {
                    textButton(viewModel.getLocalizedString("importSave")) {
                        onClickWithSound(viewModel) {
                            if (!isDisabled) {
                                viewModel.onImportClick()
                            }
                        }

                        isDisabled = state.saves.size >= MAX_SAVES_COUNT
                    }.cell(
                        width = buttonWidth,
                        height = 60f,
                        padRight = 16f,
                    )
                }

                textButton(viewModel.getLocalizedString("back")) {
                    onClickWithSound(viewModel) { viewModel.onBackClick() }
                }.cell(
                    width = buttonWidth,
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
private fun Stage.loadingList(viewModel: SinglePlayerMenuViewModel) {
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

            label(viewModel.getLocalizedString("loading"))
        }
    }
}

@Scene2dDsl
private fun Stage.loadingFailed(viewModel: SinglePlayerMenuViewModel) {
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

            label(viewModel.getLocalizedString("loadingWorldFailed"))

            row()

            textButton(viewModel.getLocalizedString("back")) {
                onClickWithSound(viewModel) { viewModel.onLoadingFailedBackClick() }
            }.cell(
                width = 400f,
                height = 60f,
                padTop = 32f,
            )
        }
    }
}

@Scene2dDsl
private fun <S> KWidget<S>.saveItem(
    viewModel: SinglePlayerMenuViewModel,
    saveInfo: SaveInfoVo,
    onLoad: () -> Unit,
    onEdit: () -> Unit,
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

            label(" - ${viewModel.getLocalizedString(saveInfo.gameMode.name.lowercase())}")
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

        textButton(viewModel.getLocalizedString("load")) {
            isDisabled = !saveInfo.isSupported

            onClickWithSound(viewModel) {
                if (isDisabled) {
                    return@onClickWithSound
                }

                onLoad()
                isDisabled = true
            }
        }

        row()

        textButton(viewModel.getLocalizedString("edit")) {
            onClickWithSound(viewModel) {
                if (isDisabled) {
                    return@onClickWithSound
                }

                onEdit()
            }
        }
    }.right()
}
