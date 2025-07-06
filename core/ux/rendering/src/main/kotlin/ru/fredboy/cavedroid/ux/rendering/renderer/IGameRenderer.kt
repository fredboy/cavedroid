package ru.fredboy.cavedroid.ux.rendering.renderer

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

interface IGameRenderer {

    val renderLayer: Int

    /**
     * When called, [spriteBatch] is beginned!
     */
    fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        delta: Float,
    )
}
