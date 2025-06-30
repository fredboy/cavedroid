package ru.fredboy.cavedroid.domain.configuration.repository

interface ApplicationContextRepository {

    fun isDebug(): Boolean

    fun isTouch(): Boolean

    fun getGameDirectory(): String

    fun getWidth(): Float

    fun getHeight(): Float

    fun setTouch(isTouch: Boolean)

    fun setGameDirectory(path: String)

    fun setWidth(width: Float)

    fun setHeight(height: Float)

}