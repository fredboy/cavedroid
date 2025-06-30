package ru.fredboy.cavedroid.domain.configuration.repository

import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext

interface GameContextRepository {

    fun isLoadGame(): Boolean

    fun shouldShowInfo(): Boolean

    fun shouldShowMap(): Boolean

    fun getJoystick(): Joystick

    fun isFullscreen(): Boolean

    fun useDynamicCamera(): Boolean

    fun getCameraContext(): CameraContext

    fun setShowInfo(show: Boolean)

    fun setShowMap(show: Boolean)

    fun setJoystick(joystick: Joystick)

    fun setFullscreen(fullscreen: Boolean)

    fun setUseDynamicCamera(use: Boolean)

    fun setCameraContext(cameraContext: CameraContext)

}
