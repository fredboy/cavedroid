package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import kotlinx.coroutines.launch
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textButton

@Scene2dDsl
fun Stage.settingsMenuView(viewModel: SettingsMenuViewModel) {
    viewModel.viewModelScope.launch {
        viewModel.settingsMenuState.collect { state ->
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

                    textButton("Screen Scale ${state.screenScale}") {
                        onClick {
                            viewModel.onScreenScaleClick()
                        }
                    }

                    row()

                    textButton("Dynamic Camera ${state.dynamicCamera}") {
                        onClick {
                            viewModel.onDynamicCameraClick()
                        }
                    }

                    row()

                    textButton("Fullscreen ${state.fullscreen}") {
                        onClick {
                            viewModel.onFullscreenClick()
                        }
                    }

                    row()

                    textButton("Auto Jump ${state.autoJump}") {
                        onClick {
                            viewModel.onAutoJumpClick()
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
}
