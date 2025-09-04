package ru.fredboy.cavedroid.game.world.generator

import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.domain.items.model.block.Block

data class WorldGeneratorConfig(
    val width: Int,
    val height: Int,
    val seed: Long,
    val minSurfaceHeight: Int,
    val maxSurfaceHeight: Int,
    val biomes: List<Biome>,
    val minBiomeSize: Int,
    val seaLevel: Int,
    val defaultBackgroundBlockKey: String,
) {

    companion object {

        fun getDefault(): WorldGeneratorConfig = WorldGeneratorConfig(
            width = 1024,
            height = 256,
            seed = TimeUtils.millis(),
            maxSurfaceHeight = 208,
            minSurfaceHeight = 128,
            biomes = Biome.entries.toList(),
            minBiomeSize = 64,
            seaLevel = 192,
            defaultBackgroundBlockKey = "stone",
        )
    }
}
