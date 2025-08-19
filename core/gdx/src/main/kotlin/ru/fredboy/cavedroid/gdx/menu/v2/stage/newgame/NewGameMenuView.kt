package ru.fredboy.cavedroid.gdx.menu.v2.stage.newgame

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textButton

@Scene2dDsl
fun Stage.newGameMenuView(viewModel: NewGameMenuViewModel) {
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

            textButton("Creative") {
                onClick {
                    viewModel.onCreativeClick()
                }
            }

            row()

            textButton("Survival") {
                onClick {
                    viewModel.onSurvivalClick()
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
