package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.audio.Sound

abstract class StepsSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getStepSound(material: String): Sound?
}
