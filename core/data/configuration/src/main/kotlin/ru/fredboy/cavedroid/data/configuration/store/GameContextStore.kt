package ru.fredboy.cavedroid.data.configuration.store

import com.badlogic.gdx.graphics.OrthographicCamera
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.data.configuration.model.GameContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameContextStore @Inject constructor() {

    private val lock = Any()

    private val gameContext = GameContext()

    var isTouch: Boolean
        get() = synchronized(lock) { gameContext.isTouch }
        set(value) = synchronized(lock) { gameContext.isTouch = value }

    var gameDirectory: String
        get() = synchronized(lock) { gameContext.gameDirectory }
        set(value) = synchronized(lock) { gameContext.gameDirectory = value }

    var width: Float
        get() = synchronized(lock) { gameContext.width }
        set(value) = synchronized(lock) { gameContext.width = value }

    var height: Float
        get() = synchronized(lock) { gameContext.height }
        set(value) = synchronized(lock) { gameContext.height = value }

    var showInfo: Boolean
        get() = synchronized(lock) { gameContext.showInfo }
        set(value) = synchronized(lock) { gameContext.showInfo = value }

    var showMap: Boolean
        get() = synchronized(lock) { gameContext.showMap }
        set(value) = synchronized(lock) { gameContext.showMap = value }

    var joystick: Joystick?
        get() = synchronized(lock) { gameContext.joystick }
        set(value) = synchronized(lock) { gameContext.joystick = value }

    var isFullscreen: Boolean
        get() = synchronized(lock) { gameContext.isFullscreen }
        set(value) = synchronized(lock) { gameContext.isFullscreen = value }

    var useDynamicCamera: Boolean
        get() = synchronized(lock) { gameContext.useDynamicCamera }
        set(value) = synchronized(lock) { gameContext.useDynamicCamera = value }

    var cameraContext: CameraContext?
        get() = synchronized(lock) { gameContext.cameraContext }
        set(value) = synchronized(lock) { gameContext.cameraContext = value }

    companion object {
        private const val TAG = "GameConfigurationStore"
    }
}
