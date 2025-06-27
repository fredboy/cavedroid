package ru.fredboy.cavedroid.data.configuration.model

import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext

internal class GameContext {

    var isTouch: Boolean = false

    var gameDirectory: String = ""

    var width: Float = 0f

    var height: Float = 0f

    var showInfo: Boolean = false

    var showMap: Boolean = false

    var joystick: Joystick? = null

    var isFullscreen: Boolean = false

    var useDynamicCamera: Boolean = false

    var cameraContext: CameraContext? = null
}
