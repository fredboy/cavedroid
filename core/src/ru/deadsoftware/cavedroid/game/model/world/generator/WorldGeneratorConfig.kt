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

        fun getDefault(): WorldGeneratorConfig {
            return WorldGeneratorConfig(
                width = 1024,
                height = 256,
                seed = TimeUtils.millis(),
                maxSurfaceHeight = 208,
                minSurfaceHeight = 128,
                biomes = Biome.entries.toList(),
                minBiomeSize = 64,
                seaLevel = 192,
            )
        }

    }

}
