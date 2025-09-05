package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

class GameWorldGenerator(
    private val config: WorldGeneratorConfig,
    private val itemsRepository: ItemsRepository,
) {

    private val random = Random(config.seed)

    private val foreMap by lazy { Array(config.width) { Array<Block>(config.height) { itemsRepository.fallbackBlock } } }
    private val backMap by lazy { Array(config.width) { Array<Block>(config.height) { itemsRepository.fallbackBlock } } }

    private val biomesMap by lazy { generateBiomes() }
    private val heights by lazy { generateHeights() }

    private val plainsPlants = listOf("dandelion", "rose", "tallgrass")
    private val mushrooms = listOf("mushroom_brown", "mushroom_red")

    private fun generateHeights(): IntArray {
        val result = IntArray(config.width)
        val noise = PerlinNoise(random)

        val scale = 1.0
        val octaves = 4
        val amplitude = (config.maxSurfaceHeight - config.minSurfaceHeight) / 2.0
        val baseHeight = (config.maxSurfaceHeight + config.minSurfaceHeight) / 2.0

        for (x in 0 until config.width) {
            val n = noise.periodicFractalNoise1D(x, config.width, scale, octaves)
            val h = (baseHeight + (n - 0.5) * 2 * amplitude).toInt()
            result[x] = h.coerceIn(config.minSurfaceHeight, config.maxSurfaceHeight)
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
        assert(x in 0..<config.width) { "x not in range of world width" }

        val surfaceHeight = heights[x]

        val grass = itemsRepository.getBlockByKey("grass_snowed")
        val bedrock = itemsRepository.getBlockByKey("bedrock")
        val dirt = itemsRepository.getBlockByKey("dirt")
        val stone = itemsRepository.getBlockByKey("stone")
        val snow = itemsRepository.getBlockByKey("snow")
        val clay = itemsRepository.getBlockByKey("clay")

        foreMap[x][surfaceHeight] = grass
        foreMap[x][config.height - 1] = bedrock
        backMap[x][surfaceHeight] = grass
        backMap[x][config.height - 1] = bedrock

        if (surfaceHeight - 1 < config.seaLevel) {
            foreMap[x][surfaceHeight - 1] = snow
        }

        for (y in min(surfaceHeight + 1, config.seaLevel)..<config.height - 1) {
            if (y <= surfaceHeight) {
                backMap[x][y] = dirt
                continue
            }

            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(
                    from = 5,
                    until = 8,
                ) -> (surfaceHeight > config.seaLevel && random.nextInt(100) < 20).ifTrue {
                    clay
                } ?: dirt

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
        assert(x in 0..<config.width) { "x not in range of world width" }

        val surfaceHeight = heights[x]

        val grass = itemsRepository.getBlockByKey("grass")
        val bedrock = itemsRepository.getBlockByKey("bedrock")
        val dirt = itemsRepository.getBlockByKey("dirt")
        val stone = itemsRepository.getBlockByKey("stone")
        val clay = itemsRepository.getBlockByKey("clay")

        foreMap[x][surfaceHeight] = grass
        foreMap[x][config.height - 1] = bedrock
        backMap[x][surfaceHeight] = grass
        backMap[x][config.height - 1] = bedrock

        for (y in min(surfaceHeight + 1, config.seaLevel)..<config.height - 1) {
            if (y <= surfaceHeight) {
                backMap[x][y] = dirt
                continue
            }

            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(
                    from = 5,
                    until = 8,
                ) -> (surfaceHeight > config.seaLevel && random.nextInt(100) < 20).ifTrue {
                    clay
                } ?: dirt

                else -> stone
            }
            backMap[x][y] = foreMap[x][y]
        }

        val plant = random.nextInt(100)
        if (surfaceHeight < config.seaLevel) {
            if (plant < 5) {
                generateOak(x)
            } else if (plant < 10) {
                generateBirch(x)
            } else if (plant < 40) {
                generateTallGrass(x)
            }
        }
    }

    private fun desertBiome(x: Int) {
        assert(x in 0..<config.width) { "x not in range of world width" }

        val surfaceHeight = heights[x]

        val sand = itemsRepository.getBlockByKey("sand")
        val bedrock = itemsRepository.getBlockByKey("bedrock")
        val sandstone = itemsRepository.getBlockByKey("sandstone")
        val stone = itemsRepository.getBlockByKey("stone")
        val clay = itemsRepository.getBlockByKey("clay")

        foreMap[x][surfaceHeight] = sand
        foreMap[x][config.height - 1] = bedrock
        backMap[x][surfaceHeight] = sand
        backMap[x][config.height - 1] = bedrock

        for (y in min(surfaceHeight + 1, config.seaLevel)..<config.height - 1) {
            if (y <= surfaceHeight) {
                backMap[x][y] = sand
                continue
            }

            foreMap[x][y] = when {
                y < surfaceHeight + random.nextInt(
                    from = 5,
                    until = 8,
                ) -> (surfaceHeight > config.seaLevel && random.nextInt(100) < 20).ifTrue {
                    clay
                } ?: sand

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

        for (x in 0..<config.width) {
            for (y in config.seaLevel..<config.height) {
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

        for (x1 in max(0, x - 1)..min(config.width - 1, x + 1)) {
            for (y in height..height + treeH - 4) {
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

    private fun generateBirch(x: Int) {
        val log = itemsRepository.getBlockByKey("log_birch")
        val leaves = itemsRepository.getBlockByKey("leaves_birch")
        val h = heights[x] - 1
        val treeH = random.nextInt(5, 7)
        val height = max(0, h - treeH)

        val top = height - 1
        if (top >= 0) {
            foreMap[x][top] = leaves
            backMap[x][top] = leaves
        }

        for (x1 in max(0, x - 1)..min(config.width - 1, x + 1)) {
            for (y in height..height + treeH - 4) {
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

        for (x1 in max(0, x - 1)..min(config.width - 1, x + 1)) {
            val y = height
            foreMap[x1][y] = leaves
            backMap[x1][y] = leaves
        }

        for (y in 1..2) {
            for (x1 in max(0, x - y)..min(config.width - 1, x + y)) {
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

    private fun generateVein(x: Int, y: Int, size: Int, block: Block) {
        val width = random.nextInt(1..size).takeIf { x + it < config.width } ?: (config.width - x - 1)
        val height = random.nextInt(1..size).takeIf { y + it < config.height - 1 } ?: (config.height - y - 2)

        for (ix in 0..width) {
            for (iy in 0..height) {
                if (random.nextDouble() < 0.5) {
                    foreMap[x + ix][y + iy] = block
                }
            }
        }
    }

    private fun generateOres() {
        val stone = itemsRepository.getBlockByKey("stone")
        val coal = itemsRepository.getBlockByKey("coal_ore")
        val iron = itemsRepository.getBlockByKey("iron_ore")
        val gold = itemsRepository.getBlockByKey("gold_ore")
        val diamond = itemsRepository.getBlockByKey("diamond_ore")
        val lapis = itemsRepository.getBlockByKey("lapis_ore")

        for (x in 0..<config.width) {
            for (y in heights[x]..<config.height) {
                val res = random.nextInt(10000)

                val h = config.height - y
                val blockAndSize = when {
                    res in 0..<25 && h < 32 -> diamond to 2
                    res in 25..<50 && h < 64 -> gold to 3
                    res in 50..<150 && h < 128 -> iron to 4
                    res in 150..<300 && h < 192 -> coal to 4
                    res in 300..<(300 + (32 - (abs(h - 16) * 2))) -> lapis to 4
                    else -> null
                }

                if (blockAndSize != null && foreMap[x][y] == stone) {
                    val (block, size) = blockAndSize
                    generateVein(x, y, size, block)
                }
            }
        }
    }

    private fun generateCaves() {
        val iterations = 5
        val threshold = 3

        val caveMap = Array(config.width) { BooleanArray(config.height) }

        for (x in 0 until config.width) {
            for (y in 0 until config.height) {
                if (y < config.minSurfaceHeight) {
                    caveMap[x][y] = true
                } else {
                    caveMap[x][y] = random.nextDouble() < 0.5
                }
            }
        }

        repeat(iterations) {
            val newMap = Array(config.width) { BooleanArray(config.height) }
            for (x in 0 until config.width) {
                for (y in heights[x] until config.height) {
                    val solidNeighbors = countSolidNeighbors(caveMap, x, y)
                    newMap[x][y] = if (solidNeighbors > threshold) true else false
                }
            }
            for (x in 0 until config.width) {
                for (y in heights[x] until config.height) {
                    caveMap[x][y] = newMap[x][y]
                }
            }
        }

        for (x in 0 until config.width) {
            for (y in heights[x] until config.height - 1) {
                if (!caveMap[x][y]) {
                    val filler = random.nextDouble()
                    foreMap[x][y] = when {
                        filler < 0.98 -> itemsRepository.fallbackBlock
                        filler < 0.99 -> itemsRepository.getBlockByKey("water")
                        else -> itemsRepository.getBlockByKey("lava")
                    }
                }
            }
        }
    }

    private fun countSolidNeighbors(map: Array<BooleanArray>, cx: Int, cy: Int): Int {
        var count = 0
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nx = cx + dx
                val ny = cy + dy
                if (nx in map.indices && ny in map[0].indices) {
                    if (map[nx][ny]) count++
                } else {
                    count++
                }
            }
        }
        return count
    }

    private fun fillLava() {
        val lava = itemsRepository.getBlockByKey("lava")

        for (x in 0 until config.width) {
            for (y in config.lavaLevel until config.height - 1) {
                val block = foreMap[x][y]
                if (block.isNone()) {
                    foreMap[x][y] = lava
                }
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
        }

        generateOres()
        fillWater()
        generateCaves()
        fillLava()

        return Pair(foreMap, backMap)
    }
}
