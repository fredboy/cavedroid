package ru.fredboy.cavedroid.gameplay.rendering.renderer

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable

interface IGameRenderer : Disposable {

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

    override fun dispose() = Unit
}
