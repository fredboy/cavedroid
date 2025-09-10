package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.audio.Sound

abstract class DropSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getDropPopSound(): Sound?
}
