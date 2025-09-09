package ru.fredboy.cavedroid.common.api

import com.badlogic.gdx.audio.Sound

interface SoundPlayer {
    fun playSoundAtPosition(sound: Sound, soundX: Float, soundY: Float, playerX: Float, playerY: Float)
}
