package ru.fredboy.cavedroid.domain.assets.repository

import ru.fredboy.cavedroid.domain.assets.model.RainSound

abstract class WeatherSoundAssetsRepository : SoundAssetsRepository() {

    abstract fun getRainSound(): RainSound?
}
