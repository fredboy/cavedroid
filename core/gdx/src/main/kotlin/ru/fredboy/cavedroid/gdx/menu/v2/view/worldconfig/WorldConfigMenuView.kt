package ru.fredboy.cavedroid.gdx.menu.v2.view.worldconfig

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.worldConfigMenuView(viewModel: WorldConfigMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        when (state) {
            is WorldConfigMenuState.Show -> show(viewModel)
            is WorldConfigMenuState.Generating -> generating(viewModel)
        }
    }
}

@Scene2dDsl
private fun Stage.generating(viewModel: WorldConfigMenuViewModel) {
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

            label(viewModel.getLocalizedString("generatingWorld"))
        }
    }
}

@Scene2dDsl
private fun Stage.show(viewModel: WorldConfigMenuViewModel) {
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

            label(viewModel.getLocalizedString("worldSize")) {
                setAlignment(Align.center)
            }.cell(
                expandX = true,
                fillX = true,
                padTop = 8f,
                padBottom = 8f,
            )

            row()

            scrollPane {
                table {
                    defaults()
                        .width(600f)
                        .height(60f)
                        .pad(10f)

                    viewModel.worldSizes.forEach { size ->
                        textButton(viewModel.getWorldSizeLabel(size)) {
                            onClickWithSound(viewModel) {
                                viewModel.onSizeClick(size)
                            }
                        }

                        row()
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

            textButton(viewModel.getLocalizedString("back")) {
                onClickWithSound(viewModel) { viewModel.onBackClick() }
            }.cell(
                width = 600f,
                height = 60f,
                padTop = 16f,
            )
        }
    }
}
