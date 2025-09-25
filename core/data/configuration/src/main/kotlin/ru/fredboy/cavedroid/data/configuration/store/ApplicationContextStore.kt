package ru.fredboy.cavedroid.data.configuration.store

import ru.fredboy.cavedroid.common.CaveDroidConstants.PreferenceKeys
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationContextStore @Inject constructor(
    private val applicationContext: ApplicationContext,
    private val preferencesStore: PreferencesStore,
) {

    private val lock = Any()

    val isDebug: Boolean
        get() = synchronized(lock) { applicationContext.isDebug }

    var isTouch: Boolean
        get() = synchronized(lock) { applicationContext.isTouch }
        set(value) = synchronized(lock) { applicationContext.isTouch = value }

    var isFullscreen: Boolean
        get() = synchronized(lock) { applicationContext.isFullscreen }
        set(value) = synchronized(lock) {
            applicationContext.isFullscreen = value
            preferencesStore.setPreference(PreferenceKeys.FULLSCREEN, value.toString())
        }

    var useDynamicCamera: Boolean
        get() = synchronized(lock) { applicationContext.useDynamicCamera }
        set(value) = synchronized(lock) {
            applicationContext.useDynamicCamera = value
            preferencesStore.setPreference(PreferenceKeys.DYNAMIC_CAMERA, value.toString())
        }

    var gameDirectory: String
        get() = synchronized(lock) { applicationContext.gameDirectory }
        set(value) = synchronized(lock) { applicationContext.gameDirectory = value }

    var width: Float
        get() = synchronized(lock) { applicationContext.width }
        set(value) = synchronized(lock) { applicationContext.width = value }

    var height: Float
        get() = synchronized(lock) { applicationContext.height }
        set(value) = synchronized(lock) { applicationContext.height = value }

    var isAutoJumpEnabled: Boolean
        get() = synchronized(lock) { applicationContext.isAutoJumpEnabled }
        set(value) = synchronized(lock) {
            applicationContext.isAutoJumpEnabled = value
            preferencesStore.setPreference(PreferenceKeys.AUTO_JUMP, value.toString())
        }

    var locale: Locale
        get() = synchronized(lock) { applicationContext.locale }
        set(value) = synchronized(lock) {
            applicationContext.locale = value
            preferencesStore.setPreference(PreferenceKeys.LOCALE, value.language)
        }

    var isSoundEnabled: Boolean
        get() = synchronized(lock) { applicationContext.soundEnabled }
        set(value) = synchronized(lock) {
            applicationContext.soundEnabled = value
            preferencesStore.setPreference(PreferenceKeys.SOUND_ENABLED, value.toString())
        }
}
