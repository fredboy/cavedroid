package ru.fredboy.cavedroid.gdx.menu.v2.view.death

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
fun Stage.deathScreenView(viewModel: DeathScreenViewModel) = viewModel.also {
    actors {
        menuButtonsTable(
            withBackground = false,
            withGameLogo = false,
            withVersion = false,
        ) {
            background(skin.getDrawable("shade_tile"))

            label(viewModel.getLocalizedString("youDied")) {
                setAlignment(Align.center)
            }.cell(
                expandX = true,
                align = Align.center,
            )

            row()

            textButton(viewModel.getLocalizedString("respawn")) {
                onClickWithSound(viewModel) {
                    viewModel.onRespawnClick()
                }
            }

            row()

            textButton(viewModel.getLocalizedString("backToMenu")) {
                onClickWithSound(viewModel) {
                    viewModel.onBackToMenuClick()
                }
            }
        }
    }
}
