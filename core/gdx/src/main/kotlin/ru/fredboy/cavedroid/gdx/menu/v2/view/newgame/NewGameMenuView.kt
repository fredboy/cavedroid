package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton
import ktx.scene2d.textField
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable

@Scene2dDsl
suspend fun Stage.newGameMenuView(viewModel: NewGameMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        when (state) {
            is NewGameMenuState.Show -> show(viewModel)
            is NewGameMenuState.Generating -> generating()
        }
    }
}

@Scene2dDsl
private fun Stage.generating() {
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

            label("Generating World...")
        }
    }
}

@Scene2dDsl
fun Stage.show(viewModel: NewGameMenuViewModel) {
    viewModel.also {
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
}
