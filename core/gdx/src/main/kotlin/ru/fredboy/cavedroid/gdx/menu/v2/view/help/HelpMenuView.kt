package ru.fredboy.cavedroid.gdx.menu.v2.view.help

import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable

@Scene2dDsl
fun Stage.helpMenuView(viewModel: HelpMenuViewModel) = viewModel.also {
    actors {
        menuButtonsTable {
            textButton(viewModel.getLocalizedString("about")) {
                onClick {
                    viewModel.onAboutClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("attribution")) {
                onClick {
                    viewModel.onAttributionClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("licenses")) {
                onClick {
                    viewModel.onLicensesClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("back")) {
                onClick {
                    viewModel.onBackClicked()
                }
            }

            row()
        }
    }
}
