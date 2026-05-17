package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.common.utils.toToggleStateString
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.settingsMenuView(viewModel: SettingsMenuViewModel) = viewModel.also {
    viewModel.stateFlow.collect { state ->
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

                label(viewModel.getLocalizedString("settings")) {
                    setAlignment(Align.center)
                }.cell(
                    expandX = true,
                    fillX = true,
                    padBottom = 8f,
                )

                row()

                scrollPane {
                    table {
                        defaults()
                            .width(600f)
                            .height(60f)
                            .pad(10f)

                        textButton(
                            viewModel.getFormattedString("dynamicCamera", state.dynamicCamera.toToggleStateString()),
                        ) {
                            onClickWithSound(viewModel) {
                                viewModel.onDynamicCameraClick(!state.dynamicCamera)
                            }
                        }

                        row()

                        if (state.showFullscreenButton) {
                            textButton(
                                viewModel.getFormattedString("fullscreen", state.fullscreen.toToggleStateString()),
                            ) {
                                onClickWithSound(viewModel) {
                                    viewModel.onFullscreenClick(!state.fullscreen)
                                }
                            }

                            row()
                        }

                        textButton(
                            viewModel.getFormattedString("autoJump", state.autoJump.toToggleStateString()),
                        ) {
                            onClickWithSound(viewModel) {
                                viewModel.onAutoJumpClick(!state.autoJump)
                            }
                        }

                        row()

                        textButton(
                            viewModel.getFormattedString("enableSound", state.sound.toToggleStateString()),
                        ) {
                            onClickWithSound(viewModel) {
                                viewModel.onSoundClick(!state.sound)
                            }
                        }

                        row()

                        if (state.showPersonalizedAdsToggle) {
                            textButton(
                                viewModel.getFormattedString(
                                    "personalizedAds",
                                    state.personalizedAds.toToggleStateString(),
                                ),
                            ) {
                                onClickWithSound(viewModel) {
                                    viewModel.onPersonalizedAdsClick(!state.personalizedAds)
                                }
                            }

                            row()
                        }

                        textButton(viewModel.getLocalizedString("resetHints")) {
                            onClickWithSound(viewModel) {
                                if (!isDisabled) {
                                    viewModel.onResetHintsClick()
                                }
                            }
                            isDisabled = !state.canResetHints
                        }

                        row()
                    }
                }.also { pane ->
                    setScrollFocus(pane)
                }.cell(
                    expand = true,
                    fill = true,
                    align = Align.center,
                )

                row()
                    .bottom()

                textButton(viewModel.getLocalizedString("done")) {
                    onClickWithSound(viewModel) {
                        viewModel.onDoneClick()
                    }
                }.cell(
                    width = 600f,
                    height = 60f,
                    padTop = 16f,
                )
            }
        }
    }
}
