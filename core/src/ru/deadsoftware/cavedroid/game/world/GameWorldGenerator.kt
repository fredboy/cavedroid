package ru.deadsoftware.cavedroid.game.world

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.model.world.Biome
import ru.deadsoftware.cavedroid.game.model.world.generator.WorldGeneratorConfig
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class GameWorldGenerator(
    private val config: WorldGeneratorConfig,
    private val gameItemsHolder: GameItemsHolder,
) {

    private val random = Random(config.seed)

    private val foreMap by lazy { Array(config.width) { Array(config.height) { gameItemsHolder.fallbackBlock } } }
    private val backMap by lazy { Array(config.width) { Array(config.height) { gameItemsHolder.fallbackBlock } } }

    private val heights by lazy { generateHeights() }
    private val biomesMap by lazy { generateBiomes() }

    private fun generateHeights(): IntArray {
        val surfaceHeightRange = config.minSurfaceHeight .. config.maxSurfaceHeight
        val result = IntArray(config.width)

        result[0] = (config.minSurfaceHeight + config.maxSurfaceHeight) / 2

        for (x in 1 ..< config.width) {
            val previous = result[x - 1]
            var d = random.nextInt(-5, 6).let { if (it !in -4..4) it / abs(it) else 0 }

            if (previous + d !in surfaceHeightRange) { d = -d }

            if (result.lastIndex - x < abs(result[0] - previous) * 3) {
                d = result[0].compareTo(previous).let { if (it != 0) it / abs(it) else 0 }
            }

            result[x] = result[x - 1] + d
        }

        return result
    }

    private fun generateBiomes(): Map<Int, Biome> {
        val xSequence = sequence {
            var lastX = 0
            var count = 0

            while (lastX < config.width - config.minBiomeSize - 1) {
                yield(lastX)

                lastX = random.nextInt(lastX + config.minBiomeSize, config.width)
                count++
            }
        }

        return xSequence.associateWith { config.biomes.random(random) }
    }

    private fun plainsBiome(x: Int) {
        assert(x in 0 ..< config.width) { "x not in range of world width" }

        val surfaceHeight = heights[x]

        val grass = gameItemsHolder.getBlock("grass")
        val bedrock = gameItemsHolder.getBlock("bedrock")
        val dirt = gameItemsHolder.getBlock("dirt")
        val stone = gameItemsHolder.getBlock("stone")

        foreMap[x][surfaceHeight] = grass
        foreMap[x][config.height - 1] = bedrock
        backMap[x][surfaceHeight] = grass
        backMap[x][config.height - 1] = bedrock

        for (y in surfaceHeight + 1 ..< config.height - 1) {
            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(5, 8) -> dirt
                else -> stone
            }
            backMap[x][y] = foreMap[x][y]
        }
    }

    private fun desertBiome(x: Int) {
        assert(x in 0 ..< config.width) { "x not in range of world width" }

        val surfaceHeight = heights[x]

        val sand = gameItemsHolder.getBlock("sand")
        val bedrock = gameItemsHolder.getBlock("bedrock")
        val sandstone = gameItemsHolder.getBlock("sandstone")
        val stone = gameItemsHolder.getBlock("stone")


        foreMap[x][surfaceHeight] = sand
        foreMap[x][config.height - 1] = bedrock
        backMap[x][surfaceHeight] = sand
        backMap[x][config.height - 1] = bedrock

        for (y in surfaceHeight + 1 ..< config.height - 1) {
            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(5, 8) -> sand
                y < surfaceHeight + random.nextInt(0, 2) -> sandstone
                else -> stone
            }
            backMap[x][y] = foreMap[x][y]
        }

        if (surfaceHeight < config.seaLevel && random.nextInt(100) < 5) {
            generateCactus(x)
        }
    }

    private fun fillWater() {
        val water = gameItemsHolder.getBlock("water")

        for (x in 0 ..< config.width) {
            for (y in config.seaLevel ..< config.height) {
                if (foreMap[x][y] != gameItemsHolder.fallbackBlock) {
                    break
                }

                foreMap[x][y] = water
            }
        }
    }

    private fun generateCactus(x: Int) {
        val cactus = gameItemsHolder.getBlock("cactus")
        val cactusHeight = random.nextInt(3)
        val h = heights[x] - 1

        for (y in h downTo max(0, h - cactusHeight)) {
            foreMap[x][y] = cactus
        }
    }

    /**
     * Generate world
     */
    fun generate(): Pair<Array<Array<Block>>, Array<Array<Block>>> {
        var biome = Biome.PLAINS

        for (x in 0 until config.width) {
            biome = biomesMap[x] ?: biome

            when (biome) {
                Biome.PLAINS -> plainsBiome(x)
                Biome.DESERT -> desertBiome(x)
            }
        }

        fillWater()

        return Pair(foreMap, backMap)
    }

}
