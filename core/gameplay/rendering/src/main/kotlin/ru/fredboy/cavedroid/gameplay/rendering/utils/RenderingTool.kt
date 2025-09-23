package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.graphics.g2d.SpriteBatch as GdxSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer as GdxShapeRenderer

sealed interface RenderingTool : Disposable {

    fun begin()

    fun end()

    fun setProjectionMatrix(matrix: Matrix4)

    class SpriteBatch : RenderingTool {

        val spriteBatch = GdxSpriteBatch()

        override fun begin() {
            spriteBatch.begin()
        }

        override fun end() {
            spriteBatch.end()
        }

        override fun setProjectionMatrix(matrix: Matrix4) {
            spriteBatch.setProjectionMatrix(matrix)
        }

        override fun dispose() {
            spriteBatch.dispose()
        }
    }

    class ShapeRenderer : RenderingTool {

        val shapeRenderer = GdxShapeRenderer()

        override fun begin() {
            shapeRenderer.begin(GdxShapeRenderer.ShapeType.Filled)
        }

        override fun end() {
            shapeRenderer.end()
        }

        override fun setProjectionMatrix(matrix: Matrix4) {
            shapeRenderer.setProjectionMatrix(matrix)
        }

        override fun dispose() {
            shapeRenderer.dispose()
        }
    }
}
