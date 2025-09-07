package ru.fredboy.cavedroid.domain.configuration.repository

import java.util.Locale

interface ApplicationContextRepository {

    fun isDebug(): Boolean

    fun isTouch(): Boolean

    fun isFullscreen(): Boolean

    fun useDynamicCamera(): Boolean

    fun getGameDirectory(): String

    fun getWidth(): Float

    fun getHeight(): Float

    fun setTouch(isTouch: Boolean)

    fun setFullscreen(fullscreen: Boolean)

    fun setUseDynamicCamera(use: Boolean)

    fun setGameDirectory(path: String)

    fun setWidth(width: Float)

    fun setHeight(height: Float)

    fun isAutoJumpEnabled(): Boolean
    fun setAutoJumpEnabled(enabled: Boolean)

    fun getLocale(): Locale

    fun setLocale(locale: Locale)

    fun getSupportedLocales(): List<Locale>
}
