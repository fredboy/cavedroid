package ru.fredboy.cavedroid.data.configuration.repository

import co.touchlab.kermit.Logger
import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.CaveDroidConstants.SUPPORTED_LOCALES
import ru.fredboy.cavedroid.data.configuration.store.ApplicationContextStore
import ru.fredboy.cavedroid.domain.configuration.model.LightingBackend
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationContextRepositoryImpl @Inject constructor(
    private val applicationContextStore: ApplicationContextStore,
) : ApplicationContextRepository {

    override fun isDebug(): Boolean = applicationContextStore.isDebug

    override fun isYandexGamesBuild(): Boolean = applicationContextStore.isYandexGamesBuild

    override fun isTouch(): Boolean = applicationContextStore.isTouch

    override fun isFullscreen(): Boolean = applicationContextStore.isFullscreen

    override fun useDynamicCamera(): Boolean = applicationContextStore.useDynamicCamera

    override fun getGameDirectory(): String = applicationContextStore.gameDirectory

    override fun getGameDirectoryFileType(): Files.FileType = applicationContextStore.gameDirectoryFileType

    override fun getWidth(): Float = applicationContextStore.width

    override fun getHeight(): Float = applicationContextStore.height

    override fun isAutoJumpEnabled(): Boolean = applicationContextStore.isAutoJumpEnabled

    override fun setTouch(isTouch: Boolean) {
        applicationContextStore.isTouch = isTouch
    }

    override fun setFullscreen(fullscreen: Boolean) {
        if (!Gdx.graphics.supportsDisplayModeChange() || fullscreen == isFullscreen()) {
            return
        }

        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else {
            Gdx.graphics.setWindowedMode(960, 540)
            setWidth(Gdx.graphics.width.toFloat() / 2)
            setHeight(Gdx.graphics.height.toFloat() / 2)
        }
        applicationContextStore.isFullscreen = fullscreen
    }

    override fun setUseDynamicCamera(use: Boolean) {
        applicationContextStore.useDynamicCamera = use
    }

    override fun setGameDirectory(path: String) {
        applicationContextStore.gameDirectory = path
    }

    override fun setWidth(width: Float) {
        applicationContextStore.width = width
    }

    override fun setHeight(height: Float) {
        applicationContextStore.height = height
    }

    override fun setAutoJumpEnabled(enabled: Boolean) {
        applicationContextStore.isAutoJumpEnabled = enabled
    }

    override fun getLocale(): Locale {
        val locale = applicationContextStore.locale

        if (locale !in SUPPORTED_LOCALES) {
            logger.e { "Locale not supported: ${locale.language}. Falling back to English" }
            return Locale.ENGLISH.also { en -> setLocale(en) }
        }

        return locale
    }

    override fun setLocale(locale: Locale) {
        if (locale !in SUPPORTED_LOCALES) {
            logger.e { "Locale not supported: ${locale.language}" }
            return
        }

        applicationContextStore.locale = locale
    }

    override fun getSupportedLocales(): List<Locale> {
        return SUPPORTED_LOCALES
    }

    override fun isSoundEnabled(): Boolean {
        return applicationContextStore.isSoundEnabled
    }

    override fun setSoundEnabled(enabled: Boolean) {
        applicationContextStore.isSoundEnabled = enabled
    }

    override fun isOnboardingShown(): Boolean = applicationContextStore.isOnboardingShown

    override fun setOnboardingShown(shown: Boolean) {
        applicationContextStore.isOnboardingShown = shown
    }

    override fun isInventoryHintShown(): Boolean = applicationContextStore.isInventoryHintShown

    override fun setInventoryHintShown(shown: Boolean) {
        applicationContextStore.isInventoryHintShown = shown
    }

    override fun getPersonalizedAdsConsent(): Boolean? = applicationContextStore.personalizedAdsConsent

    override fun setPersonalizedAdsConsent(consent: Boolean?) {
        applicationContextStore.personalizedAdsConsent = consent
    }

    override fun getLightingBackend(): LightingBackend = applicationContextStore.lightingBackend

    override fun setLightingBackend(backend: LightingBackend) {
        applicationContextStore.lightingBackend = backend
    }

    override var preferShowDebug: Boolean
        get() = applicationContextStore.preferShowDebug
        set(value) {
            applicationContextStore.preferShowDebug = value
        }

    companion object {
        private val logger = Logger.withTag("ApplicationContextRepositoryImpl")
    }
}
