package ru.fredboy.cavedroid.data.configuration.model

import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.common.model.WorldType
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext

class GameContext(
    internal val isLoadGame: Boolean,
    internal var saveGameDirectory: String,
    internal val worldName: String,
    internal val requestedWorldWidth: Int?,
    internal val requestedSeed: Long?,
    internal val worldType: WorldType,
    internal var showInfo: Boolean,
    internal var showMap: Boolean,
    internal var joystick: Joystick,
    internal var cameraContext: CameraContext,
)
