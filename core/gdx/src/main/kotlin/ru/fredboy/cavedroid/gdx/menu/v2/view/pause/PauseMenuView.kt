package ru.fredboy.cavedroid.gdx.menu.v2.view.pause

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable

@Scene2dDsl
fun Stage.pauseMenuView(viewModel: PauseMenuViewModel) = viewModel.also {
    actors {
        menuButtonsTable(
            withBackground = false,
            withGameLogo = false,
            withVersion = false,
        ) {
            background(skin.getDrawable("shade_tile"))

            label(viewModel.getLocalizedString("pauseMenu")) {
                setAlignment(Align.center)
            }.cell(
                expandX = true,
                align = Align.center,
            )

            row()

            textButton(viewModel.getLocalizedString("resumeGame")) {
                onClick {
                    viewModel.onResumeClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("settings")) {
                onClick {
                    viewModel.onSettingsClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("quit")) {
                onClick {
                    viewModel.onQuitGameClick()
                }
            }
        }
    }
}
