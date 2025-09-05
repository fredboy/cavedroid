package ru.fredboy.cavedroid.gdx.menu.v2.view.common

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import ktx.scene2d.KTableWidget
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.table
import ru.fredboy.cavedroid.common.CaveDroidConstants

@Scene2dDsl
fun <S> KWidget<S>.menuButtonsTable(
    skin: Skin = Scene2DSkin.defaultSkin,
    withBackground: Boolean = true,
    withGameLogo: Boolean = true,
    withVersion: Boolean = true,
    init: KTableWidget.() -> Unit = {},
): KTableWidget {
    return actor(KTableWidget(skin)) {
        setFillParent(true)

        if (withBackground) {
            background(
                TiledDrawable(
                    TextureRegionDrawable(
                        skin.getRegion("background"),
                    ),
                ),
            )
        }

        pad(8f)

        if (withGameLogo) {
            image("gamelogo").apply {
                cell(
                    width = 600f,
                    height = 600f * drawable.minHeight / drawable.minWidth,
                )
                center()
                top()
            }
        }

        row()
            .expandY()
            .center()

        table {
            defaults()
                .width(600f)
                .height(60f)
                .pad(10f)

            init()
        }

        row()
            .bottom()
            .left()
            .expandX()

        if (withVersion) {
            label("CaveDroid ${CaveDroidConstants.VERSION}")
        }
    }
}
