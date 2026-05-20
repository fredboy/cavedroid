package ru.fredboy.cavedroid.game.world.generator

import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.domain.world.model.Biome

data class WorldGeneratorConfig(
    val width: Int,
    val height: Int,
    val seed: Long,
    val minSurfaceHeight: Int,
    val maxSurfaceHeight: Int,
    val biomes: List<Biome>,
    val minBiomeSize: Int,
    val seaLevel: Int,
    val lavaLevel: Int,
    val defaultBackgroundBlockKey: String,
) {

    companion object {

        fun getDefault(): WorldGeneratorConfig = WorldGeneratorConfig(
            width = 1024,
            height = 256,
            seed = TimeUtils.millis(),
            maxSurfaceHeight = 160,
            minSurfaceHeight = 64,
            biomes = listOf(
                Biome.PLAINS,
                Biome.PLAINS,
                Biome.DESERT,
                Biome.WINTER,
            ),
            minBiomeSize = 64,
            seaLevel = 128,
            lavaLevel = 224,
            defaultBackgroundBlockKey = "stone",
        )
    }
}
