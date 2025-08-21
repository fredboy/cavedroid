package ru.fredboy.cavedroid.gdx.menu.v2.view.help

import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable

@Scene2dDsl
fun Stage.helpMenuView(viewModel: HelpMenuViewModel) {
    actors {
        menuButtonsTable {
            textButton("About") {
                onClick {
                    viewModel.onAboutClick()
                }
            }

            row()

            textButton("Attribution") {
                onClick {
                    viewModel.onAttributionClick()
                }
            }

            row()

            textButton("Licenses") {
                onClick {
                    viewModel.onLicensesClick()
                }
            }

            row()

            textButton("Back") {
                onClick {
                    viewModel.onBackClicked()
                }
            }

            row()
        }
    }
}
