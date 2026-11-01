package ru.fredboy.cavedroid.gdx.menu.v2.view.assetpacks

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.common.mvvm.collectViewState
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.assetPacksView(viewModel: AssetPacksViewModel) = viewModel.also {
    collectViewState(viewModel.state) { state ->
        actors {
            table {
                setFillParent(true)
                background(TiledDrawable(TextureRegionDrawable(skin.getRegion("background"))))
                pad(12f)
                label(viewModel.getLocalizedString("assetPacks")).cell(padBottom = 8f)
                row()
                if (state.confirmRemove != null) {
                    label(viewModel.getLocalizedString("packsRemoveConfirm")) { setWrap(true) }.cell(width = 650f, pad = 12f)
                    row()
                    textButton(viewModel.getLocalizedString("packsConfirm")) {
                        onClickWithSound(viewModel) { viewModel.confirm(true) }
                    }.cell(width = 500f, height = 60f, pad = 8f)
                    row()
                    textButton(viewModel.getLocalizedString("back")) {
                        onClickWithSound(viewModel) { viewModel.confirm(false) }
                    }.cell(width = 500f, height = 60f, pad = 8f)
                } else {
                    state.message?.let { message ->
                        label(message) { setWrap(true) }.cell(width = 700f, pad = 8f)
                        row()
                    }
                    if (state.busy) {
                        label(viewModel.getLocalizedString("packsWorking")).cell(pad = 8f)
                        row()
                    }
                    scrollPane {
                        name = "assetpacks-content"
                        table {
                            val ordered = state.selected.mapNotNull { id -> state.packs.find { it.id == id } } +
                                state.packs.filter { it.id !in state.selected }
                            ordered.forEach { pack ->
                                table {
                                    viewModel.icon(pack)?.let { icon ->
                                        image(icon) { setScaling(Scaling.fit) }.cell(width = 64f, height = 64f, padRight = 12f)
                                    }
                                    table {
                                        defaults().left()
                                        label("${pack.metadata.name}  ${pack.metadata.version}") { setWrap(true) }.cell(width = 600f)
                                        row()
                                        label("${pack.metadata.author} · ${pack.metadata.license}") { setWrap(true) }.cell(width = 600f)
                                        row()
                                        label(viewModel.getFormattedString("packsFormatVersion", pack.metadata.formatVersion.toString()))
                                        row()
                                        val status = if (pack.id in viewModel.runningIds) "packsRunning" else "packsNotRunning"
                                        label(viewModel.getLocalizedString(status))
                                    }
                                }.cell(padTop = 14f, align = Align.left)
                                row()
                                table {
                                    defaults().height(48f).pad(4f)
                                    textButton(viewModel.getLocalizedString(if (pack.id in state.selected) "packsDisable" else "packsEnable")) {
                                        isDisabled = state.busy
                                        onClickWithSound(viewModel) { viewModel.toggle(pack) }
                                    }.cell(width = 180f)
                                    if (pack.id in state.selected) {
                                        textButton(viewModel.getLocalizedString("packsUp")) {
                                            isDisabled = state.busy || state.selected.first() == pack.id
                                            onClickWithSound(viewModel) { viewModel.move(pack.id, -1) }
                                        }.cell(width = 150f)
                                        textButton(viewModel.getLocalizedString("packsDown")) {
                                            isDisabled = state.busy || state.selected.last() == pack.id
                                            onClickWithSound(viewModel) { viewModel.move(pack.id, 1) }
                                        }.cell(width = 150f)
                                    }
                                    textButton(viewModel.getLocalizedString("packsRemove")) {
                                        isDisabled = state.busy
                                        onClickWithSound(viewModel) { viewModel.requestRemove(pack.id) }
                                    }.cell(width = 150f)
                                }
                                row()
                            }
                            label(viewModel.getLocalizedString("packsBundled")).cell(pad = 16f)
                        }
                    }.cell(expand = true, fill = true)
                    row()
                    table {
                        defaults().width(220f).height(60f).pad(6f)
                        textButton(viewModel.getLocalizedString("packsImport")) {
                            isDisabled = state.busy
                            onClickWithSound(viewModel) { viewModel.importPack() }
                        }
                        textButton(viewModel.getLocalizedString("done")) {
                            isDisabled = state.busy
                            onClickWithSound(viewModel) { viewModel.done() }
                        }
                    }
                }
            }
        }
    }
}
