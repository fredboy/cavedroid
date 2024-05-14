package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.ksp.annotations.GenerateSetMultibindingsModule

@GenerateSetMultibindingsModule(
    interfaceClass = IGameRenderer::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.render",
    moduleName = "RenderModule"
)
annotation class GameRenderer

interface IGameRenderer {

    val renderLayer: Int

    /**
     * When called, [spriteBatch] is beginned!
     */
    fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        delta: Float
    )
}