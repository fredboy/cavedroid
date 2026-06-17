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

        const val DEFAULT_WIDTH: Int = 1024

        fun getDefault(width: Int = DEFAULT_WIDTH, seed: Long = TimeUtils.millis()): WorldGeneratorConfig = WorldGeneratorConfig(
            width = width,
            height = 256,
            seed = seed,
            maxSurfaceHeight = 160,
            minSurfaceHeight = 64,
            biomes = listOf(
                Biome.DESERT,
                Biome.PLAINS,
                Biome.PLAINS,
                Biome.WINTER,
            ),
            minBiomeSize = 64,
            seaLevel = 128,
            lavaLevel = 224,
            defaultBackgroundBlockKey = "stone",
        )
    }
}
