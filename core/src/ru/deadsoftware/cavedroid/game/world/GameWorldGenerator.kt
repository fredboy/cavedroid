package ru.deadsoftware.cavedroid.game.world

import com.badlogic.gdx.utils.TimeUtils
import ru.deadsoftware.cavedroid.game.GameItems
import kotlin.math.abs
import kotlin.random.Random

object GameWorldGenerator {

    private const val BIOME_MIN_SIZE = 64

    private enum class Biome {
        PLAINS,
        DESERT
    }

    private fun generateHeights(width: Int, min: Int, max: Int, random: Random) = IntArray(width).apply {
        set(0, (min + max) / 2)
        for (x in 1 until width) {
            val previous = get(x - 1)
            var d = random.nextInt(-5, 6).let { if (it !in -4..4) it / abs(it) else 0 }

            if (previous + d !in min..max) { d = -d }
            if (lastIndex - x < abs(get(0) - previous) * 3) {
                d = get(0).compareTo(previous).let { if (it != 0) it / abs(it) else 0 }
            }

            set(x, get(x - 1) + d)
        }
    }

    private fun generateBiomes(width: Int, random: Random) = buildMap<Int, Biome> {
        val xSequence = sequence {
            var lastX = 0
            var count = 0

            while (lastX < width - BIOME_MIN_SIZE - 1) {
                yield(lastX)

                lastX = random.nextInt(lastX + BIOME_MIN_SIZE, width)
                count++
            }
        }

        return xSequence.associateWith { Biome.values()[random.nextInt(Biome.values().size)] }
    }

    private fun plainsBiome(
        foreMap: Array<IntArray>,
        backMap: Array<IntArray>,
        width: Int,
        height: Int,
        x: Int,
        xHeight: Int,
        random: Random,
    ) {
        foreMap[x][xHeight] = GameItems.getBlockId("grass")
        foreMap[x][height - 1] = GameItems.getBlockId("bedrock")
        backMap[x][xHeight] = GameItems.getBlockId("grass")
        backMap[x][height - 1] = GameItems.getBlockId("bedrock")

        for (y in xHeight + 1 until height - 1) {
            foreMap[x][y] = when {
                y < xHeight + random.nextInt(5, 8) -> GameItems.getBlockId("dirt")
                else -> GameItems.getBlockId("stone")
            }
            backMap[x][y] = foreMap[x][y]
        }
    }

    private fun desertBiome(
        foreMap: Array<IntArray>,
        backMap: Array<IntArray>,
        width: Int,
        height: Int,
        x: Int,
        xHeight: Int,
        random: Random,
    ) {
        foreMap[x][xHeight] = GameItems.getBlockId("sand")
        foreMap[x][height - 1] = GameItems.getBlockId("bedrock")
        backMap[x][xHeight] = GameItems.getBlockId("sand")
        backMap[x][height - 1] = GameItems.getBlockId("bedrock")

        for (y in xHeight + 1 until height - 1) {
            foreMap[x][y] = when {
                y < xHeight + random.nextInt(5, 8) -> GameItems.getBlockId("sand")
                else -> GameItems.getBlockId("stone")
            }
            backMap[x][y] = foreMap[x][y]
        }
    }

    private fun fillWater(foreMap: Array<IntArray>, width: Int, height: Int, waterLevel: Int) {
        for (x in 0 until width) {
            for (y in waterLevel until height) {
                if (foreMap[x][y] != 0) {
                    break
                }

                foreMap[x][y] = GameItems.getBlockId("water")
            }
        }
    }

    /**
     * Generates world of given width and height with given seed
     * @param width world width
     * @param height world height
     * @param seed seed for random number generator
     * @return pair of foreground and background layers
     */
    fun generate(width: Int, height: Int, seed: Long = TimeUtils.millis()): Pair<Array<IntArray>, Array<IntArray>> {
        val random = Random(seed)
        val foreMap = Array(width) { IntArray(height) }
        val backMap = Array(width) { IntArray(width) }
        val heightsMap = generateHeights(width, height / 4, height * 3 / 4, random)
        val biomesMap = generateBiomes(width, random)

        var biome = Biome.PLAINS

        for (x in 0 until width) {
            val xHeight = heightsMap[x]
            biome = biomesMap[x] ?: biome

            when (biome) {
                Biome.PLAINS -> plainsBiome(foreMap, backMap, width, height, x, xHeight, random)
                Biome.DESERT -> desertBiome(foreMap, backMap, width, height, x, xHeight, random)
            }
        }

        fillWater(foreMap, width, height, height / 2)

        return Pair(foreMap, backMap)
    }

}
