package ru.fredboy.cavedroid.data.assets.internal

import co.touchlab.kermit.Logger
import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.domain.assets.model.RainSound
import ru.fredboy.cavedroid.domain.assets.repository.WeatherSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WeatherSoundAssetsRepositoryImpl @Inject constructor() : WeatherSoundAssetsRepository() {

    private var rainSounds = emptyList<RainSound>()

    override fun initialize() {
        val weatherDir = assetResolver.handle(WEATHER_DIRECTORY)
        val indexFile = assetResolver.handle(INDEX_FILE)

        if (!indexFile.exists()) {
            return
        }

        indexFile.readString().lineSequence().map(String::trim).filter(String::isNotEmpty).forEach { kind ->
            if (kind.isBlank()) return@forEach
            val kindDir = weatherDir.child(kind).takeIf { it.exists() } ?: return@forEach
            if (kind != RAIN_KEY) return@forEach
            rainSounds = buildList {
                var index = 0
                while (true) {
                    val file = kindDir.child("${index++}.wav").takeIf { it.exists() } ?: break
                    val thunder = runCatching {
                        ThunderDetector.analyze(assetResolver.resolve(file.path()).read())
                    }.onFailure { logger.w(it) { "Cannot detect thunder in ${file.path()}" } }.getOrNull()
                    // Rain tracks exceed Android SoundPool's decoded sample limit.
                    add(RainSound(Gdx.audio.newMusic(assetResolver.resolve(file.path())), thunder))
                }
            }
        }
    }

    override fun getRainSound(): RainSound? {
        return rainSounds.randomOrNull()
    }

    override fun dispose() {
        rainSounds.forEach { it.music.dispose() }
        super.dispose()
        rainSounds = emptyList()
    }

    companion object {
        private val logger = Logger.withTag("WeatherSounds")
        private const val WEATHER_DIRECTORY = "sfx/weather"

        private const val INDEX_FILE = "$WEATHER_DIRECTORY/index.txt"

        private const val RAIN_KEY = "rain"
    }
}
