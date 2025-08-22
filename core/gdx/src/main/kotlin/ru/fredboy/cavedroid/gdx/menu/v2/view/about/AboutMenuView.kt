package ru.fredboy.cavedroid.gdx.menu.v2.view.about

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Align
import ktx.actors.onClick
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actors
import ktx.scene2d.image
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton
import ru.fredboy.cavedroid.common.CaveDroidConstants

@Scene2dDsl
fun Stage.aboutMenuView(viewModel: AboutMenuViewModel) = viewModel.also {
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

            image("gamelogo").apply {
                cell(
                    width = 600f,
                    height = 600f * drawable.minHeight / drawable.minWidth,
                )
                center()
                top()
            }

            row()

            label(
                """
                    ${CaveDroidConstants.TITLE} ${CaveDroidConstants.VERSION}

                    MIT License

                    Copyright © 2018-2025 Fedor Ivanov <fredboy@protonmail.com>
                """.trimIndent(),
            ) {
                wrap = true
                setAlignment(Align.center)
            }.cell(
                expand = true,
                fill = true,
                align = Align.center,
            )

            row()
                .bottom()

            table {
                textButton("Go to GitHub") {
                    onClick { viewModel.onGithubClick() }
                }.cell(
                    width = 400f,
                    height = 60f,
                    padRight = 16f,
                )

                textButton("Back") {
                    onClick { viewModel.onBackClick() }
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
