package ru.fredboy.cavedroid.gdx.menu.v2.view.main

import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable

@Scene2dDsl
fun Stage.mainMenuView(viewModel: MainMenuViewModel) {
    actors {
        menuButtonsTable {
            textButton("New Game") {
                onClick {
                    viewModel.onNewGameClick()
                }
            }

            row()

            textButton("Load Game") {
                onClick {
                    viewModel.onLoadGameClick()
                }
            }

            row()

            textButton("Settings") {
                onClick {
                    viewModel.onSettingsClick()
                }
            }

            row()

            textButton("Exit") {
                onClick {
                    viewModel.onExitGameClick()
                }
            }

            row()
        }
    }
}
