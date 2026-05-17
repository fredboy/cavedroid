package ru.fredboy.cavedroid.domain.configuration.repository

import com.badlogic.gdx.Files
import java.util.Locale

interface ApplicationContextRepository {

    fun isDebug(): Boolean

    fun isYandexGamesBuild(): Boolean

    fun isTouch(): Boolean

    fun isFullscreen(): Boolean

    fun useDynamicCamera(): Boolean

    fun getGameDirectory(): String

    fun getGameDirectoryFileType(): Files.FileType

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

    fun setSoundEnabled(enabled: Boolean)

    fun isSoundEnabled(): Boolean

    fun isOnboardingShown(): Boolean

    fun setOnboardingShown(shown: Boolean)

    fun isInventoryHintShown(): Boolean

    fun setInventoryHintShown(shown: Boolean)

    fun getPersonalizedAdsConsent(): Boolean?

    fun setPersonalizedAdsConsent(consent: Boolean?)
}
