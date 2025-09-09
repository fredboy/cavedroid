package ru.fredboy.cavedroid.gdx.menu.v2.view.main

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.imageButton
import ktx.scene2d.table
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
fun Stage.mainMenuView(viewModel: MainMenuViewModel) = viewModel.also {
    actors {
        menuButtonsTable {
            textButton(viewModel.getLocalizedString("play")) {
                onClickWithSound(viewModel) {
                    viewModel.onSinglePlayerClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("settings")) {
                onClickWithSound(viewModel) {
                    viewModel.onSettingsClick()
                }
            }

            row()
                .expandX()

            table {
                imageButton {
                    image("lang")
                        .cell(
                            width = 40f,
                            height = 40f,
                        )

                    onClickWithSound(viewModel) { viewModel.onLanguageClick() }
                }.cell(
                    width = 60f,
                    height = 60f,
                    align = Align.right,
                )

                textButton(viewModel.getLocalizedString("exit")) {
                    onClickWithSound(viewModel) {
                        viewModel.onExitGameClick()
                    }
                }.cell(
                    width = 600f,
                    height = 60f,
                    pad = 10f,
                )

                imageButton {
                    image("help")
                        .cell(
                            width = 40f,
                            height = 40f,
                        )

                    onClickWithSound(viewModel) {
                        viewModel.onHelpClick()
                    }
                }.cell(
                    width = 60f,
                    height = 60f,
                    align = Align.right,
                )
            }
        }
    }
}
