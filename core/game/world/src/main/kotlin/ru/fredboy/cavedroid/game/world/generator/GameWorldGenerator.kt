package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameWorldGenerator(
    private val config: WorldGeneratorConfig,
    private val itemsRepository: ItemsRepository,
) {

    private val random = Random(config.seed)

    private val foreMap by lazy { Array(config.width) { Array<Block>(config.height) { itemsRepository.fallbackBlock } } }
    private val backMap by lazy { Array(config.width) { Array<Block>(config.height) { itemsRepository.fallbackBlock } } }

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

        val grass = itemsRepository.getBlockByKey("grass_snowed")
        val bedrock = itemsRepository.getBlockByKey("bedrock")
        val dirt = itemsRepository.getBlockByKey("dirt")
        val stone = itemsRepository.getBlockByKey("stone")
        val snow = itemsRepository.getBlockByKey("snow")

        foreMap[x][surfaceHeight] = grass
        foreMap[x][config.height - 1] = bedrock
        backMap[x][surfaceHeight] = grass
        backMap[x][config.height - 1] = bedrock

        if (surfaceHeight - 1 < config.seaLevel) {
            foreMap[x][surfaceHeight - 1] = snow
        }

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
                generateSpruce(x)
            }
        }
    }

    private fun plainsBiome(x: Int) {
        assert(x in 0 ..< config.width) { "x not in range of world width" }

        val surfaceHeight = heights[x]

        val grass = itemsRepository.getBlockByKey("grass")
        val bedrock = itemsRepository.getBlockByKey("bedrock")
        val dirt = itemsRepository.getBlockByKey("dirt")
        val stone = itemsRepository.getBlockByKey("stone")

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

        val sand = itemsRepository.getBlockByKey("sand")
        val bedrock = itemsRepository.getBlockByKey("bedrock")
        val sandstone = itemsRepository.getBlockByKey("sandstone")
        val stone = itemsRepository.getBlockByKey("stone")


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
        val water = itemsRepository.getBlockByKey("water")

        for (x in 0 ..< config.width) {
            for (y in config.seaLevel ..< config.height) {
                if (!foreMap[x][y].isNone()) {
                    break
                }

                foreMap[x][y] = water
            }
        }
    }

    private fun generateCactus(x: Int) {
        val cactus = itemsRepository.getBlockByKey("cactus")
        val cactusHeight = random.nextInt(3)
        val h = heights[x] - 1

        for (y in h downTo max(0, h - cactusHeight)) {
            foreMap[x][y] = cactus
        }
    }

    private fun generateOak(x: Int) {
        val log = itemsRepository.getBlockByKey("log_oak")
        val leaves = itemsRepository.getBlockByKey("leaves_oak")
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
                foreMap[x1][heights[x1] - 1] = itemsRepository.getBlockByKey(mushrooms.random(random))
            }
        }

        for (y in h downTo height) {
            backMap[x][y] = log
        }
    }

    private fun generateSpruce(x: Int) {
        val log = itemsRepository.getBlockByKey("log_spruce")
        val leaves = itemsRepository.getBlockByKey("leaves_spruce")
        val h = heights[x] - 1
        val treeH = random.nextInt(7, 9)
        val height = max(0, h - treeH)

        val top = height - 1
        if (top >= 0) {
            foreMap[x][top] = leaves
            backMap[x][top] = leaves
        }

        for (x1 in max(0, x - 1) .. min(config.width - 1, x + 1)) {
            val y = height
            foreMap[x1][y] = leaves
            backMap[x1][y] = leaves
        }

        for (y in 1..2) {
            for (x1 in max(0, x - y) .. min(config.width - 1, x + y)) {
                foreMap[x1][height + 1 + y] = leaves
                backMap[x1][height + 1 + y] = leaves
            }
        }

        for (y in h downTo height) {
            backMap[x][y] = log
        }
    }

    private fun generateTallGrass(x: Int) {
        val tallGrass = itemsRepository.getBlockByKey(plainsPlants.random(random))
        val h = heights[x] - 1
        if (h > 0) {
            foreMap[x][h] = tallGrass
        }
    }

    private fun generateDeadBush(x: Int) {
        val bush = itemsRepository.getBlockByKey("deadbush")
        val h = heights[x] - 1
        if (h > 0) {
            foreMap[x][h] = bush
        }
    }

    private fun generateOres(x : Int) {
        val stone = itemsRepository.getBlockByKey("stone")
        val coal = itemsRepository.getBlockByKey("coal_ore")
        val iron = itemsRepository.getBlockByKey("iron_ore")
        val gold = itemsRepository.getBlockByKey("gold_ore")
        val diamond = itemsRepository.getBlockByKey("diamond_ore")
        val lapis = itemsRepository.getBlockByKey("lapis_ore")

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
