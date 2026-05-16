package ru.fredboy.cavedroid.gdx.menu.v2.view.notice

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
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.onClickWithSound

@Scene2dDsl
suspend fun Stage.noticeMenuView(viewModel: NoticeMenuViewModel) = viewModel.also {
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

                scrollPane {
                    label(state.notices) { wrap = true }
                }.cell(
                    expand = true,
                    fill = true,
                    align = Align.center,
                )

                row()
                    .bottom()

                textButton(viewModel.getLocalizedString("back")) {
                    onClickWithSound(viewModel) { viewModel.onBackClick() }
                }.cell(
                    width = 600f,
                    height = 60f,
                    pad = 16f,
                )
            }
        }
    }
}
