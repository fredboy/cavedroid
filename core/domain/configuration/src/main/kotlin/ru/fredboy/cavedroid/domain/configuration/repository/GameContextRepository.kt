package ru.fredboy.cavedroid.domain.configuration.repository

import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext

interface GameContextRepository {

    fun getSaveGameDirectory(): String

    fun setSaveGameDirectory(directory: String)

    fun getWorldName(): String

    fun isLoadGame(): Boolean

    fun shouldShowInfo(): Boolean

    fun shouldShowMap(): Boolean

    fun getJoystick(): Joystick

    fun getCameraContext(): CameraContext

    fun setShowInfo(show: Boolean)

    fun setShowMap(show: Boolean)

    fun setJoystick(joystick: Joystick)

    fun setCameraContext(cameraContext: CameraContext)
}
