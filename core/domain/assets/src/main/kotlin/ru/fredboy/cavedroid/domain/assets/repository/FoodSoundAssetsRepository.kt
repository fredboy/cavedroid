package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.audio.Sound

abstract class FoodSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getFoodSound(): Sound?
}
