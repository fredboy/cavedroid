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
            is NewGameMenuState.Generating -> generating(viewModel)
        }
    }
}

@Scene2dDsl
private fun Stage.generating(viewModel: NewGameMenuViewModel) {
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
fun Stage.show(viewModel: NewGameMenuViewModel) {
    viewModel.also {
        actors {
            menuButtonsTable {
                val worldNameField = textField(viewModel.getLocalizedString("newWorld"))

                row()

                textButton(viewModel.getLocalizedString("creative")) {
                    onClick {
                        viewModel.onCreativeClick(worldNameField.text)
                    }
                }

                row()

                textButton(viewModel.getLocalizedString("survival")) {
                    onClick {
                        viewModel.onSurvivalClick(worldNameField.text)
                    }
                }

                row()

                textButton(viewModel.getLocalizedString("back")) {
                    onClick {
                        viewModel.onBackClick()
                    }
                }

                row()
            }
        }
    }
}
