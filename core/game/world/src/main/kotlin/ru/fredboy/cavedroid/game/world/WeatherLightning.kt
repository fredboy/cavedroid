package ru.fredboy.cavedroid.game.world

import ru.fredboy.cavedroid.common.di.GameScope
import javax.inject.Inject

/** Presentation state shared by the rain playback controller and world renderer. */
@GameScope
class WeatherLightning @Inject constructor() {
    var intensity: Float = 0f
}
