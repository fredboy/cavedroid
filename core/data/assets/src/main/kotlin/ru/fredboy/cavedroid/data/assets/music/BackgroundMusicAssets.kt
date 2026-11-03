package ru.fredboy.cavedroid.data.assets.music

import com.badlogic.gdx.utils.JsonReader
import ru.fredboy.cavedroid.data.assets.pack.PackFormat
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundMusicAssets @Inject constructor(private val resolver: AssetResolver) {
    val trackDurations: Map<String, Float> by lazy {
        val index = JsonReader().parse(resolver.handle(INDEX_PATH).readString("UTF-8"))
        require(index.isObject) { "$INDEX_PATH must be an object mapping track paths to durations" }
        val tracks = linkedMapOf<String, Float>()
        index.forEach { entry ->
            val track = PackFormat.path(entry.name)
            require(entry.isNumber) { "$INDEX_PATH: duration for $track must be a number" }
            val duration = entry.asFloat()
            require(duration.isFinite() && duration > 0f) { "$INDEX_PATH: invalid duration for $track" }
            require(track !in tracks) { "$INDEX_PATH: duplicate track $track" }
            val file = resolver.resolve("bgm/$track")
            require(file.exists() && !file.isDirectory) { "$INDEX_PATH: missing music file bgm/$track" }
            file.read().use { input ->
                require(input.read() != -1) { "$INDEX_PATH: empty music file bgm/$track" }
            }
            tracks[track] = duration
        }
        tracks
    }

    fun validate() {
        trackDurations
    }

    companion object {
        const val INDEX_PATH = "bgm/index.json"
    }
}
