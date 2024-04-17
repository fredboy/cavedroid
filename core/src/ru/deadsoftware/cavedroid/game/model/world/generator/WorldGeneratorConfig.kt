package ru.deadsoftware.cavedroid.game.model.world.generator

import com.badlogic.gdx.utils.TimeUtils
import ru.deadsoftware.cavedroid.game.model.world.Biome

data class WorldGeneratorConfig(
    val width: Int,
    val height: Int,
    val seed: Long,
    val minSurfaceHeight: Int,
    val maxSurfaceHeight: Int,
    val biomes: List<Biome>,
    val minBiomeSize: Int,
    val seaLevel: Int,
) {

    companion object {
        private const val DEFAULT_WIDTH = 1024
        private const val DEFAULT_HEIGHT = 256
        private const val DEFAULT_MIN_BIOME_SIZE = 64

        fun getDefaultWithSeed(): WorldGeneratorConfig {
            return WorldGeneratorConfig(
                width = DEFAULT_WIDTH,
                height = DEFAULT_HEIGHT,
                seed = TimeUtils.millis(),
                minSurfaceHeight = DEFAULT_HEIGHT / 4,
                maxSurfaceHeight = DEFAULT_HEIGHT * 3 / 4,
                biomes = Biome.entries.toList(),
                minBiomeSize = DEFAULT_MIN_BIOME_SIZE,
                seaLevel = DEFAULT_HEIGHT / 2,
            )
        }

    }

}
