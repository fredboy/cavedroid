package ru.fredboy.cavedroid.domain.configuration.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Rectangle

data class CameraContext(
    val viewport: Rectangle,
    val camera: Camera,
) {

    fun xOnViewport(x: Int) = viewport.width / Gdx.graphics.width * x.toFloat()

    fun yOnViewport(y: Int) = viewport.height / Gdx.graphics.height * y.toFloat()

    fun getViewportCoordinates(x: Int, y: Int): Pair<Float, Float> {
        return xOnViewport(x) to yOnViewport(y)
    }
}