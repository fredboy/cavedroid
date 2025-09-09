package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.common.utils.toToggleStateString
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.settingsMenuView(viewModel: SettingsMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
        actors {
            menuButtonsTable(
                withGameLogo = false,
                withVersion = false,
            ) {
                label(viewModel.getLocalizedString("settings")) {
                    setAlignment(Align.center)
                }.cell(
                    expandX = true,
                    align = Align.center,
                )

                row()

                textButton(viewModel.getFormattedString("dynamicCamera", state.dynamicCamera.toToggleStateString())) {
                    onClickWithSound(viewModel) {
                        viewModel.onDynamicCameraClick(!state.dynamicCamera)
                    }
                }

                row()

                if (Gdx.graphics.supportsDisplayModeChange()) {
                    textButton(viewModel.getFormattedString("fullscreen", state.fullscreen.toToggleStateString())) {
                        onClickWithSound(viewModel) {
                            viewModel.onFullscreenClick(!state.fullscreen)
                        }
                    }

                    row()
                }

                textButton(viewModel.getFormattedString("autoJump", state.autoJump.toToggleStateString())) {
                    onClickWithSound(viewModel) {
                        viewModel.onAutoJumpClick(!state.autoJump)
                    }
                }

                row()

                textButton(viewModel.getFormattedString("enableSound", state.sound.toToggleStateString())) {
                    onClickWithSound(viewModel) {
                        viewModel.onSoundClick(!state.sound)
                    }
                }

                row()

                textButton(viewModel.getLocalizedString("done")) {
                    onClickWithSound(viewModel) {
                        viewModel.onDoneClick()
                    }
                }

                row()
            }
        }
    }
}
