package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.textButton
import ktx.scene2d.textField
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable

@Scene2dDsl
fun Stage.newGameMenuView(viewModel: NewGameMenuViewModel) = viewModel.also {
    actors {
        menuButtonsTable {
            val worldNameField = textField("New World")

            row()

            textButton("Creative") {
                onClick {
                    viewModel.onCreativeClick(worldNameField.text)
                }
            }

            row()

            textButton("Survival") {
                onClick {
                    viewModel.onSurvivalClick(worldNameField.text)
                }
            }

            row()

            textButton("Back") {
                onClick {
                    viewModel.onBackClick()
                }
            }

            row()
        }
    }
}
