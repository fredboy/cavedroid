package ru.fredboy.cavedroid.data.configuration.store

import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.data.configuration.model.GameConfiguration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameConfigurationStore @Inject constructor() {

    private val lock = Any()

    private val gameConfiguration = GameConfiguration()

    var isTouch: Boolean
        get() = synchronized(lock) { gameConfiguration.isTouch }
        set(value) = synchronized(lock) { gameConfiguration.isTouch = value }

    var gameDirectory: String
        get() = synchronized(lock) { gameConfiguration.gameDirectory }
        set(value) = synchronized(lock) { gameConfiguration.gameDirectory = value }

    var width: Float
        get() = synchronized(lock) { gameConfiguration.width }
        set(value) = synchronized(lock) { gameConfiguration.width = value }

    var height: Float
        get() = synchronized(lock) { gameConfiguration.height }
        set(value) = synchronized(lock) { gameConfiguration.height = value }

    var showInfo: Boolean
        get() = synchronized(lock) { gameConfiguration.showInfo }
        set(value) = synchronized(lock) { gameConfiguration.showInfo = value }

    var showMap: Boolean
        get() = synchronized(lock) { gameConfiguration.showMap }
        set(value) = synchronized(lock) { gameConfiguration.showMap = value }

    var joystick: Joystick?
        get() = synchronized(lock) { gameConfiguration.joystick }
        set(value) = synchronized(lock) { gameConfiguration.joystick = value }

    var isFullscreen: Boolean
        get() = synchronized(lock) { gameConfiguration.isFullscreen }
        set(value) = synchronized(lock) { gameConfiguration.isFullscreen = value }

    var useDynamicCamera: Boolean
        get() = synchronized(lock) { gameConfiguration.useDynamicCamera }
        set(value) = synchronized(lock) { gameConfiguration.useDynamicCamera = value }

    companion object {
        private const val TAG = "GameConfigurationStore"
    }
}
