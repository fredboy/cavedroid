package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import ru.fredboy.cavedroid.domain.assets.repository.WeatherSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WeatherSoundAssetsRepositoryImpl @Inject constructor() : WeatherSoundAssetsRepository() {

    private val soundsMap = mutableMapOf<String, List<Sound>>()

    override fun initialize() {
        val weatherDir = Gdx.files.internal(WEATHER_DIRECTORY)
        val indexFile = Gdx.files.internal(INDEX_FILE)

        if (!indexFile.exists()) {
            return
        }

        indexFile.readString().split("\n").forEach { kind ->
            if (kind.isBlank()) return@forEach
            val kindDir = weatherDir.child(kind).takeIf { it.exists() } ?: return@forEach
            soundsMap[kind] = kindDir.loadAllSounds()
        }
    }

    override fun getRainSound(): Sound? {
        return soundsMap[RAIN_KEY]?.takeIf { it.isNotEmpty() }?.random()
    }

    override fun dispose() {
        super.dispose()
        soundsMap.clear()
    }

    companion object {
        private const val WEATHER_DIRECTORY = "sfx/weather"

        private const val INDEX_FILE = "$WEATHER_DIRECTORY/index.txt"

        private const val RAIN_KEY = "rain"
    }
}
