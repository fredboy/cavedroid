package ru.fredboy.cavedroid.gdx.menu.v2.view.disclaimer

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
fun Stage.adsDisclaimerView(viewModel: AdsDisclaimerViewModel) = viewModel.also {
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

            label(viewModel.getLocalizedString("adsDisclaimerTitle")) {
                setAlignment(Align.center)
            }.cell(
                expandX = true,
                fillX = true,
                padBottom = 8f,
            )

            row()

            scrollPane {
                label(viewModel.getLocalizedString("adsDisclaimerBody")) { wrap = true }
            }.cell(
                expand = true,
                fill = true,
                align = Align.center,
            )

            row()
                .bottom()

            table {
                textButton(viewModel.getLocalizedString("adsDisclaimerAgree")) {
                    onClickWithSound(viewModel) { viewModel.onAgreeClick() }
                }.cell(
                    width = 400f,
                    height = 60f,
                    padRight = 16f,
                )

                textButton(viewModel.getLocalizedString("adsDisclaimerOptOut")) {
                    onClickWithSound(viewModel) { viewModel.onOptOutClick() }
                }.cell(
                    width = 400f,
                    height = 60f,
                    padLeft = 16f,
                )
            }.cell(
                expandX = true,
                fillX = true,
                padTop = 16f,
            )
        }
    }
}
