package ru.fredboy.cavedroid.gdx.menu.v2.view.main

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textButton

@Scene2dDsl
fun Stage.mainMenuView(viewModel: MainMenuViewModel) {
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
