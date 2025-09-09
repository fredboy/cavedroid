package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import kotlin.math.sqrt

private const val HEARING_DISTANCE = 50f

fun playSoundAtPosition(sound: Sound, soundX: Float, soundY: Float, playerX: Float, playerY: Float) {
    val maxHearingDistance = HEARING_DISTANCE
    val dx = soundX - playerX
    val dy = soundY - playerY
    val distance = MathUtils.clamp(sqrt((dx * dx + dy * dy).toDouble()).toFloat(), 0f, maxHearingDistance)

    val volume = 1.0f - (distance / maxHearingDistance)

    val pan = MathUtils.clamp(dx / maxHearingDistance, -1f, 1f)

    sound.play(volume, 1f, pan)
}
