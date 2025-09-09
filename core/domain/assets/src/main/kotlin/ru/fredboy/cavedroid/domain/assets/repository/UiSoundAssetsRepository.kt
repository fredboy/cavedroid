package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.audio.Sound

abstract class UiSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getClickSound(): Sound
}
