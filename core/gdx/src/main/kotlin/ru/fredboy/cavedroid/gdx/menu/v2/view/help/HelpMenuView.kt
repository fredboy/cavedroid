package ru.fredboy.cavedroid.gdx.menu.v2.view.help

import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
fun Stage.helpMenuView(viewModel: HelpMenuViewModel) = viewModel.also {
    actors {
        menuButtonsTable {
            textButton(viewModel.getLocalizedString("about")) {
                onClickWithSound(viewModel) {
                    viewModel.onAboutClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("attribution")) {
                onClickWithSound(viewModel) {
                    viewModel.onAttributionClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("licenses")) {
                onClickWithSound(viewModel) {
                    viewModel.onLicensesClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("back")) {
                onClickWithSound(viewModel) {
                    viewModel.onBackClicked()
                }
            }

            row()
        }
    }
}
