package ru.fredboy.cavedroid.data.configuration.store

import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
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
            preferencesStore.setPreference(KEY_FULLSCREEN_PREF, value.toString())
        }

    var useDynamicCamera: Boolean
        get() = synchronized(lock) { applicationContext.useDynamicCamera }
        set(value) = synchronized(lock) {
            applicationContext.useDynamicCamera = value
            preferencesStore.setPreference(KEY_DYNAMIC_CAMERA_PREF, value.toString())
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

    var screenScale: Int
        get() = synchronized(lock) { applicationContext.screenScale }
        set(value) = synchronized(lock) {
            applicationContext.screenScale = value
            preferencesStore.setPreference(KEY_SCREEN_SCALE_PREF, value.toString())
        }

    private companion object {
        private const val KEY_FULLSCREEN_PREF = "fullscreen"
        private const val KEY_DYNAMIC_CAMERA_PREF = "dyncam"
        private const val KEY_SCREEN_SCALE_PREF = "screen_scale"
    }
}
