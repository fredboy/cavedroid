package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.audio.Sound

abstract class WeatherSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getRainSound(): Sound?
}
