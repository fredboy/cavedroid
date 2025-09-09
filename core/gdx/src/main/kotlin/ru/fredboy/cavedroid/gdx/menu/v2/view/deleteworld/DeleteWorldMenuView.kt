package ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.deleteWorldMenuView(viewModel: DeleteWorldMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        when (state) {
            is DeleteWorldMenuState.Deleting -> {
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

                        label(viewModel.getLocalizedString("deletingWorld"))
                    }
                }
            }

            is DeleteWorldMenuState.ConfirmDeleting -> {
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

                        label(viewModel.getFormattedString("deleteWorldWithName", state.worldName)) {
                            setAlignment(Align.center)
                        }.cell(
                            align = Align.center,
                        )

                        row()

                        table {
                            textButton(viewModel.getLocalizedString("confirm")) {
                                onClickWithSound(viewModel) { viewModel.onConfirmClick() }
                            }.cell(
                                width = 400f,
                                height = 60f,
                                padRight = 16f,
                            )

                            textButton(viewModel.getLocalizedString("cancel")) {
                                onClickWithSound(viewModel) { viewModel.onCancelClick() }
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
    }
}
