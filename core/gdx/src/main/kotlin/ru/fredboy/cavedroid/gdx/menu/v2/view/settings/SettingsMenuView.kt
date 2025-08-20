package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.common.utils.toToggleStateString
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.menuButtonsTable

@Scene2dDsl
suspend fun Stage.settingsMenuView(viewModel: SettingsMenuViewModel) {
    viewModel.stateFlow.collect { state ->
        actors {
            menuButtonsTable {
                textButton("Dynamic Camera: ${state.dynamicCamera.toToggleStateString()}") {
                    onClick {
                        viewModel.onDynamicCameraClick(!state.dynamicCamera)
                    }
                }

                row()

                if (Gdx.graphics.supportsDisplayModeChange()) {
                    textButton("Fullscreen: ${state.fullscreen.toToggleStateString()}") {
                        onClick {
                            viewModel.onFullscreenClick(!state.fullscreen)
                        }
                    }

                    row()
                }

                textButton("Auto Jump: ${state.autoJump.toToggleStateString()}") {
                    onClick {
                        viewModel.onAutoJumpClick(!state.autoJump)
                    }
                }

                row()

                textButton("Done") {
                    onClick {
                        viewModel.onDoneClick()
                    }
                }

                row()
            }
        }
    }
}
