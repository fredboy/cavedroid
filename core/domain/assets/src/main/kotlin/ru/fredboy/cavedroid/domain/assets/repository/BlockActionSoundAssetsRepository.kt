package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.audio.Sound

abstract class BlockActionSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getBlockActionSound(key: String): Sound?
}
