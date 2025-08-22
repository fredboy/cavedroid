package ru.fredboy.cavedroid.gdx.menu.v2.view.attribution

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.scrollPane
import ktx.scene2d.table
import ktx.scene2d.textButton

@Scene2dDsl
fun Stage.attributionMenuView(viewModel: AttributionMenuViewModel) = viewModel.also {
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
                label(
                    """
                        Attributions

                        - Pixel Perfection by XSSheep – Creative Commons Attribution-ShareAlike 4.0
                            https://creativecommons.org/licenses/by-sa/4.0/

                        - On-screen Joystick – CC0 from OpenGameArt
                            https://opengameart.org/content/mmorpg-virtual-joysticks

                        - Minecraft Font by JDGraphics – Public Domain
                            https://www.fontspace.com/minecraft-font-f28180
                    """.trimIndent(),
                ) {
                    wrap = true
                }
            }.cell(
                expand = true,
                fill = true,
                align = Align.center,
            )

            row()
                .bottom()

            textButton("Back") {
                onClick { viewModel.onBackClick() }
            }.cell(
                width = 600f,
                height = 60f,
                pad = 16f,
            )
        }
    }
}
