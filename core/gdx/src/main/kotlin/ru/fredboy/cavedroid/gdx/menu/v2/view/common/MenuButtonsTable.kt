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
import ru.fredboy.cavedroid.common.utils.dp

@Scene2dDsl
fun <S> KWidget<S>.menuButtonsTable(
    skin: Skin = Scene2DSkin.defaultSkin,
    init: KTableWidget.() -> Unit = {},
): KTableWidget {
    return actor(KTableWidget(skin)) {
        setFillParent(true)
        background(
            TiledDrawable(
                TextureRegionDrawable(
                    skin.getRegion("background"),
                ),
            ),
        )

        image("gamelogo").apply {
            cell(
                width = 600.dp,
                height = 600.dp * drawable.minHeight / drawable.minWidth,
            )
            center()
            top()
        }

        row()
            .expandY()
            .center()

        table {
            defaults()
                .width(600.dp)
                .height(60.dp)
                .pad(10.dp)

            init()
        }

        row()
            .bottom()
            .left()
            .expandX()

        label("CaveDroid ${CaveDroidConstants.VERSION}")
    }
}
