package ru.fredboy.cavedroid.data.configuration.store

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.data.configuration.model.GameContext
import javax.inject.Inject

@GameScope
class GameContextStore @Inject constructor(
    private val gameContext: GameContext
) {

    private val lock = Any()

    val isLoadGame: Boolean
        get() = gameContext.isLoadGame

    var showInfo: Boolean
        get() = synchronized(lock) { gameContext.showInfo }
        set(value) = synchronized(lock) { gameContext.showInfo = value }

    var showMap: Boolean
        get() = synchronized(lock) { gameContext.showMap }
        set(value) = synchronized(lock) { gameContext.showMap = value }

    var joystick: Joystick
        get() = synchronized(lock) { gameContext.joystick }
        set(value) = synchronized(lock) { gameContext.joystick = value }

    var isFullscreen: Boolean
        get() = synchronized(lock) { gameContext.isFullscreen }
        set(value) = synchronized(lock) { gameContext.isFullscreen = value }

    var useDynamicCamera: Boolean
        get() = synchronized(lock) { gameContext.useDynamicCamera }
        set(value) = synchronized(lock) { gameContext.useDynamicCamera = value }

    var cameraContext: CameraContext
        get() = synchronized(lock) { gameContext.cameraContext }
        set(value) = synchronized(lock) { gameContext.cameraContext = value }

    companion object {
        private const val TAG = "GameConfigurationStore"
    }
}
