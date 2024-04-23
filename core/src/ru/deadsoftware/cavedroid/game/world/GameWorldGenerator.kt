package ru.deadsoftware.cavedroid.game.world

import com.google.common.primitives.Ints.min
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

    private val plainsPlants = listOf("dandelion", "rose", "tallgrass")
    private val mushrooms = listOf("mushroom_brown", "mushroom_red",)

    private fun generateHeights(): IntArray {
        val surfaceHeightRange = config.minSurfaceHeight .. config.maxSurfaceHeight
        val result = IntArray(config.width)

        result[0] = surfaceHeightRange.random(random)

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

    private fun winterBiome(x: Int) {
        assert(x in 0 ..< config.width) { "x not in range of world width" }

        val surfaceHeight = heights[x]

        val grass = gameItemsHolder.getBlock("grass_snowed")
        val bedrock = gameItemsHolder.getBlock("bedrock")
        val dirt = gameItemsHolder.getBlock("dirt")
        val stone = gameItemsHolder.getBlock("stone")

        foreMap[x][surfaceHeight] = grass
        foreMap[x][config.height - 1] = bedrock
        backMap[x][surfaceHeight] = grass
        backMap[x][config.height - 1] = bedrock

        for (y in min(surfaceHeight + 1, config.seaLevel) ..< config.height - 1) {
            if (y <= surfaceHeight) {
                backMap[x][y] = dirt
                continue
            }

            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(5, 8) -> dirt
                else -> stone
            }
            backMap[x][y] = foreMap[x][y]
        }
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

        for (y in min(surfaceHeight + 1, config.seaLevel) ..< config.height - 1) {
            if (y <= surfaceHeight) {
                backMap[x][y] = dirt
                continue
            }

            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(5, 8) -> dirt
                else -> stone
            }
            backMap[x][y] = foreMap[x][y]
        }

        val plant = random.nextInt(100)
        if (surfaceHeight < config.seaLevel) {
            if (plant < 10) {
                generateOak(x)
            } else if (plant < 40) {
                generateTallGrass(x)
            }
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

        for (y in min(surfaceHeight + 1, config.seaLevel) ..< config.height - 1) {
            if (y <= surfaceHeight) {
                backMap[x][y] = sand
                continue
            }

            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(5, 8) -> sand
                y < surfaceHeight + random.nextInt(0, 2) -> sandstone
                else -> stone
            }
            backMap[x][y] = foreMap[x][y]
        }

        val plant = random.nextInt(100)
        if (surfaceHeight < config.seaLevel) {
            if (plant < 5) {
                generateCactus(x)
            } else if (plant < 10) {
                generateDeadBush(x)
            }
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

    private fun generateOak(x: Int) {
        val log = gameItemsHolder.getBlock("log_oak")
        val leaves = gameItemsHolder.getBlock("leaves_oak")
        val h = heights[x] - 1
        val treeH = random.nextInt(5, 7)
        val height = max(0, h - treeH)

        val top = height - 1
        if (top >= 0) {
            foreMap[x][top] = leaves
            backMap[x][top] = leaves
        }

        for (x1 in max(0, x - 1) .. min(config.width - 1, x + 1)) {
            for (y in height .. height + treeH - 4) {
                foreMap[x1][y] = leaves
                backMap[x1][y] = leaves
            }
            if (random.nextInt(15) < 3) {
                foreMap[x1][heights[x1] - 1] = gameItemsHolder.getBlock(mushrooms.random(random))
            }
        }

        for (y in h downTo height) {
            backMap[x][y] = log
        }
    }

    private fun generateTallGrass(x: Int) {
        val tallGrass = gameItemsHolder.getBlock(plainsPlants.random(random))
        val h = heights[x] - 1
        if (h > 0) {
            foreMap[x][h] = tallGrass
        }
    }

    private fun generateDeadBush(x: Int) {
        val bush = gameItemsHolder.getBlock("deadbush")
        val h = heights[x] - 1
        if (h > 0) {
            foreMap[x][h] = bush
        }
    }

    private fun generateOres(x : Int) {
        val stone = gameItemsHolder.getBlock("stone")
        val coal = gameItemsHolder.getBlock("coal_ore")
        val iron = gameItemsHolder.getBlock("iron_ore")
        val gold = gameItemsHolder.getBlock("gold_ore")
        val diamond = gameItemsHolder.getBlock("diamond_ore")
        val lapis = gameItemsHolder.getBlock("lapis_ore")

        for (y in heights[x] ..< config.height) {
            val res = random.nextInt(10000)

            val h = config.height - y
            val block = when {
                res in 0..<25 && h < 16 -> diamond
                res in 25 ..< 50 && h < 32 -> gold
                res in 50 ..< 250 && h < 64 -> iron
                res in 250 ..< 450 && h < 128 -> coal
                res in 450 ..< (450 + (25 - (abs(h - 16) * (25 / 16)))) -> lapis
                else -> null
            }

            if (block != null && foreMap[x][y] == stone) {
                foreMap[x][y] = block
            }
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
                Biome.WINTER -> winterBiome(x)
            }

            generateOres(x)
        }

        fillWater()

        return Pair(foreMap, backMap)
    }

}
