package ru.fredboy.cavedroid.domain.configuration.repository

interface ApplicationContextRepository {

    fun isDebug(): Boolean

    fun isTouch(): Boolean

    fun isFullscreen(): Boolean

    fun useDynamicCamera(): Boolean

    fun getGameDirectory(): String

    fun getWidth(): Float

    fun getHeight(): Float

    fun getScreenScale(): Int

    fun setTouch(isTouch: Boolean)

    fun setFullscreen(fullscreen: Boolean)

    fun setUseDynamicCamera(use: Boolean)

    fun setGameDirectory(path: String)

    fun setWidth(width: Float)

    fun setHeight(height: Float)

    fun setScreenScale(scale: Int)
}
