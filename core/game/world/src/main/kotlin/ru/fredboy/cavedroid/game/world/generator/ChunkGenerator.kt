package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Deterministic, locality-bounded generator for a single [CHUNK_W]-wide column slice of an
 * infinite world.
 *
 * Every decision is a pure function of `(seed, absoluteX[, y])` so that chunks generated
 * independently agree across their shared seams. This is what lets the world be streamed: a chunk
 * looks identical whether it is generated on its own or alongside its neighbours.
 *
 * Generation runs in deterministic stages over an overscan band (the chunk plus [OVERSCAN]
 * columns on each side) so cross-chunk features (trees, ore veins, sugar cane) write consistently:
 *
 *  1. base terrain + water fill (per column)
 *  2. ore veins (per origin column; veins spill right/down within the band)
 *  3. caves + lava (per local column; carving is column-local)
 *  4. surface features (per origin column; trees/plants spill ±1)
 *  5. support cleanup (per local column)
 *
 * Only cells landing inside the requested chunk are returned; the overscan exists purely so a
 * feature originating just outside the chunk still writes the part that reaches into it.
 *
 * Unlike [GameWorldGenerator] (which does whole-world passes with a single streaming RNG and
 * periodic noise for the looping world), this generator is unbounded and seam-free. It reuses the
 * same block palette and feature shapes but reorganises them to be position-pure; output is not
 * intended to be identical to the looping generator.
 */
class ChunkGenerator(
    private val config: WorldGeneratorConfig,
    private val itemsRepository: ItemsRepository,
) {

    private val fallback get() = itemsRepository.fallbackBlock

    private fun block(key: String): Block = itemsRepository.getBlockByKey(key)

    private val grass by lazy { block("grass") }
    private val grassSnowed by lazy { block("grass_snowed") }
    private val bedrock by lazy { block("bedrock") }
    private val dirt by lazy { block("dirt") }
    private val stone by lazy { block("stone") }
    private val snow by lazy { block("snow") }
    private val clay by lazy { block("clay") }
    private val sand by lazy { block("sand") }
    private val sandstone by lazy { block("sandstone") }
    private val water by lazy { block("water") }
    private val lava by lazy { block("lava") }
    private val web by lazy { block("web") }
    private val cactus by lazy { block("cactus") }
    private val deadbush by lazy { block("deadbush") }
    private val sugarCane by lazy { block("sugar_cane") }

    private val plainsPlants = listOf("dandelion", "rose", "tallgrass")
    private val mushrooms = listOf("mushroom_brown", "mushroom_red")

    private val heightNoise = PerlinNoise(Random(config.seed))
    private val caveNoise = PerlinNoise(Random(config.seed xor CAVE_NOISE_SALT))

    private val height get() = config.height

    data class GeneratedChunk(
        val foreMap: Array<Array<Block>>,
        val backMap: Array<Array<Block>>,
        val biomes: Array<Biome>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GeneratedChunk) return false
            return foreMap.contentDeepEquals(other.foreMap) &&
                backMap.contentDeepEquals(other.backMap) &&
                biomes.contentEquals(other.biomes)
        }

        override fun hashCode(): Int {
            var result = foreMap.contentDeepHashCode()
            result = 31 * result + backMap.contentDeepHashCode()
            result = 31 * result + biomes.contentHashCode()
            return result
        }
    }

    /**
     * Generates the chunk at index [chunkX] (covering world columns `[chunkX * CHUNK_W, +CHUNK_W)`).
     */
    fun generateChunk(chunkX: Int): GeneratedChunk {
        val startX = chunkX * CHUNK_W
        val bandStart = startX - OVERSCAN
        val bandW = CHUNK_W + 2 * OVERSCAN

        val foreBand = Array(bandW) { Array(height) { fallback } }
        val backBand = Array(bandW) { Array(height) { fallback } }
        val surfaceHeights = IntArray(bandW) { surfaceHeight(bandStart + it) }
        val bandBiomes = Array(bandW) { biomeAt(bandStart + it) }

        // 1. base terrain + water for every band column
        for (j in 0 until bandW) {
            fillColumn(bandStart + j, surfaceHeights[j], bandBiomes[j], foreBand[j], backBand[j])
        }

        // 2. ore veins (origins anywhere in the band may reach into the chunk)
        for (j in 0 until bandW) {
            applyOres(bandStart + j, j, surfaceHeights[j], foreBand, bandW)
        }

        // 3. caves + lava on the local columns (column-local, no horizontal spill)
        for (i in 0 until CHUNK_W) {
            val j = OVERSCAN + i
            carveCavesAndFillLava(bandStart + j, surfaceHeights[j], foreBand[j])
        }

        // 4. surface features (trees, plants, cactus, sugar cane)
        for (j in 0 until bandW) {
            applyFeatures(bandStart + j, j, surfaceHeights, bandBiomes, foreBand, backBand, bandW)
        }

        // 5. support cleanup on local columns
        for (i in 0 until CHUNK_W) {
            val j = OVERSCAN + i
            cleanupColumn(surfaceHeights[j], foreBand[j])
        }

        val fore = Array(CHUNK_W) { foreBand[OVERSCAN + it] }
        val back = Array(CHUNK_W) { backBand[OVERSCAN + it] }
        val biomes = Array(CHUNK_W) { bandBiomes[OVERSCAN + it] }

        return GeneratedChunk(fore, back, biomes)
    }

    // region pure position helpers

    /** Surface height at world column [x] — pure, non-periodic fractal noise. */
    fun surfaceHeight(x: Int): Int {
        val amplitude = (config.maxSurfaceHeight - config.minSurfaceHeight) / 2.0
        val baseHeight = (config.maxSurfaceHeight + config.minSurfaceHeight) / 2.0
        val n = fractalNoise1D(x)
        val h = (baseHeight + (n - 0.5) * 2 * amplitude).toInt()
        return h.coerceIn(config.minSurfaceHeight, config.maxSurfaceHeight)
    }

    private fun fractalNoise1D(x: Int): Double {
        var total = 0.0
        var frequency = 1.0
        var amplitude = 1.0
        var maxValue = 0.0
        for (o in 0 until HEIGHT_OCTAVES) {
            total += heightNoise.noise2D(x * HEIGHT_SCALE * frequency, HEIGHT_NOISE_Y) * amplitude
            maxValue += amplitude
            amplitude *= HEIGHT_PERSISTENCE
            frequency *= HEIGHT_LACUNARITY
        }
        return total / maxValue
    }

    /** Biome at world column [x] — pure, deterministic per [WorldGeneratorConfig.minBiomeSize] cell. */
    fun biomeAt(x: Int): Biome {
        val cell = Math.floorDiv(x, config.minBiomeSize)
        val r = Random(columnSeed(cell, BIOME_SALT)).nextDouble()
        val index = (r * config.biomes.size).toInt().coerceIn(0, config.biomes.lastIndex)
        return config.biomes[index]
    }

    private fun columnSeed(x: Int, salt: Long): Long {
        var h = config.seed xor salt
        h = h * MIX_A + x.toLong() * MIX_B
        h = h xor (h ushr 29)
        h *= MIX_C
        h = h xor (h ushr 32)
        return h
    }

    private fun columnRandom(x: Int, salt: Long): Random = Random(columnSeed(x, salt))

    // endregion

    private fun fillColumn(x: Int, surfaceHeight: Int, biome: Biome, fore: Array<Block>, back: Array<Block>) {
        val rng = columnRandom(x, TERRAIN_SALT)

        val surfaceBlock = when (biome) {
            Biome.WINTER -> grass
            Biome.DESERT -> sand
            Biome.PLAINS -> grass
        }
        val subSurface = if (biome == Biome.DESERT) sand else dirt

        fore[surfaceHeight] = if (biome == Biome.WINTER) grassSnowed else surfaceBlock
        fore[height - 1] = bedrock
        back[surfaceHeight] = fore[surfaceHeight]
        back[height - 1] = bedrock

        if (biome == Biome.WINTER && surfaceHeight - 1 in 0 until config.seaLevel) {
            fore[surfaceHeight - 1] = snow
        }

        for (y in min(surfaceHeight + 1, config.seaLevel) until height - 1) {
            if (y <= surfaceHeight) {
                back[y] = subSurface
                continue
            }

            val dirtDepth = rng.nextInt(5, 8)
            fore[y] = when {
                y < surfaceHeight + dirtDepth -> {
                    val clayRoll = surfaceHeight > config.seaLevel && rng.nextInt(100) < 20
                    when {
                        clayRoll -> clay
                        biome == Biome.DESERT && y < surfaceHeight + 3 -> sand
                        biome == Biome.DESERT -> sandstone
                        else -> subSurface
                    }
                }

                else -> stone
            }
            back[y] = fore[y]
        }

        // water fill from sea level down to the first solid block
        for (y in config.seaLevel until height) {
            if (!fore[y].isNone()) break
            fore[y] = water
        }
    }

    private fun applyOres(x: Int, jBand: Int, surfaceHeight: Int, foreBand: Array<Array<Block>>, bandW: Int) {
        val rng = columnRandom(x, ORE_SALT)
        val coal = block("coal_ore")
        val iron = block("iron_ore")
        val gold = block("gold_ore")
        val diamond = block("diamond_ore")
        val gravel = block("gravel")
        val lapis = block("lapis_ore")

        for (y in surfaceHeight until height) {
            val res = rng.nextInt(10000)
            val depth = config.height - y
            val blockAndSize = when {
                res in 0 until 50 && depth < 32 -> diamond to 2
                res in 50 until 100 && depth < 64 -> gold to 3
                res in 100 until 250 && depth < 128 -> iron to 3
                res in 250 until 400 && depth in 33 until 192 -> coal to 3
                res in 400 until 500 -> dirt to 4
                res in 500 until 600 -> gravel to 4
                res in 600 until 600 + (32 - abs(depth - 16) * 2) -> lapis to 4
                else -> null
            }

            if (blockAndSize != null && foreBand[jBand][y] == stone) {
                val (oreBlock, size) = blockAndSize
                generateVein(jBand, y, size, oreBlock, foreBand, bandW, rng)
            }
        }
    }

    private fun generateVein(
        jBand: Int,
        y: Int,
        size: Int,
        oreBlock: Block,
        foreBand: Array<Array<Block>>,
        bandW: Int,
        rng: Random,
    ) {
        val w = min(rng.nextInt(1..size), bandW - jBand - 1)
        val h = min(rng.nextInt(1..size), height - y - 2)

        for (ix in 0..w) {
            for (iy in 0..h) {
                if (rng.nextDouble() < 0.5) {
                    foreBand[jBand + ix][y + iy] = oreBlock
                }
            }
        }
    }

    private fun carveCavesAndFillLava(x: Int, surfaceHeight: Int, fore: Array<Block>) {
        val rng = columnRandom(x, CAVE_SPECKLE_SALT)
        val caveTop = surfaceHeight + SURFACE_CAVE_BUFFER

        for (y in caveTop until height - 1) {
            val current = fore[y]
            if (current.isFluid() || !current.params.hasCollision) continue

            val n = caveNoise.noise2D(x * CAVE_SCALE_X, y * CAVE_SCALE_Y)
            if (n > CAVE_THRESHOLD) {
                val filler = rng.nextDouble()
                fore[y] = when {
                    filler < 0.95 -> fallback
                    filler < 0.98 -> web
                    filler < 0.99 -> water
                    else -> lava
                }
            }
        }

        for (y in config.lavaLevel until height - 1) {
            if (fore[y].isNone()) {
                fore[y] = lava
            }
        }
    }

    private fun applyFeatures(
        x: Int,
        jBand: Int,
        surfaceHeights: IntArray,
        bandBiomes: Array<Biome>,
        foreBand: Array<Array<Block>>,
        backBand: Array<Array<Block>>,
        bandW: Int,
    ) {
        val surfaceHeight = surfaceHeights[jBand]
        if (surfaceHeight >= config.seaLevel) return

        val rng = columnRandom(x, FEATURE_SALT)
        val plant = rng.nextInt(100)

        when (bandBiomes[jBand]) {
            Biome.PLAINS -> when {
                plant < 5 -> generateTree(jBand, surfaceHeights, foreBand, backBand, bandW, rng, "log_oak", "leaves_oak", 5, 7)
                plant < 10 -> generateTree(jBand, surfaceHeights, foreBand, backBand, bandW, rng, "log_birch", "leaves_birch", 5, 7)
                plant < 40 -> placeOnSurface(jBand, surfaceHeight, foreBand, bandW, block(plainsPlants.random(rng)))
            }

            Biome.DESERT -> when {
                plant < 5 -> generateCactus(jBand, surfaceHeight, foreBand, bandW, rng)
                plant < 10 -> placeOnSurface(jBand, surfaceHeight, foreBand, bandW, deadbush)
            }

            Biome.WINTER -> if (plant < 10) {
                generateSpruce(jBand, surfaceHeights, foreBand, backBand, bandW, rng)
            }
        }

        applySugarCane(jBand, surfaceHeight, foreBand, bandW, rng)
    }

    private fun placeOnSurface(jBand: Int, surfaceHeight: Int, foreBand: Array<Array<Block>>, bandW: Int, value: Block) {
        if (jBand !in 0 until bandW) return
        val h = surfaceHeight - 1
        if (h > 0 && foreBand[jBand][h].isNone()) {
            foreBand[jBand][h] = value
        }
    }

    private fun generateCactus(jBand: Int, surfaceHeight: Int, foreBand: Array<Array<Block>>, bandW: Int, rng: Random) {
        val cactusHeight = rng.nextInt(3)
        val h = surfaceHeight - 1
        for (y in h downTo max(0, h - cactusHeight)) {
            if (foreBand[jBand][y].isNone()) foreBand[jBand][y] = cactus
        }
    }

    private fun generateTree(
        jBand: Int,
        surfaceHeights: IntArray,
        foreBand: Array<Array<Block>>,
        backBand: Array<Array<Block>>,
        bandW: Int,
        rng: Random,
        logKey: String,
        leavesKey: String,
        minHeight: Int,
        maxHeight: Int,
    ) {
        val log = block(logKey)
        val leaves = block(leavesKey)
        val h = surfaceHeights[jBand] - 1
        val treeH = rng.nextInt(minHeight, maxHeight)
        val canopyTop = max(0, h - treeH)

        val top = canopyTop - 1
        if (top >= 0) {
            setBoth(jBand, top, leaves, foreBand, backBand, bandW)
        }

        for (jj in (jBand - 1)..(jBand + 1)) {
            if (jj !in 0 until bandW) continue
            for (y in canopyTop..canopyTop + treeH - 4) {
                setBoth(jj, y, leaves, foreBand, backBand, bandW)
            }
            if (rng.nextInt(15) < 3) {
                val surfaceTop = surfaceHeights[jj] - 1
                if (surfaceTop in 0 until height) {
                    foreBand[jj][surfaceTop] = block(mushrooms.random(rng))
                }
            }
        }

        for (y in h downTo canopyTop) {
            if (jBand in 0 until bandW) backBand[jBand][y] = log
        }
    }

    private fun generateSpruce(
        jBand: Int,
        surfaceHeights: IntArray,
        foreBand: Array<Array<Block>>,
        backBand: Array<Array<Block>>,
        bandW: Int,
        rng: Random,
    ) {
        val log = block("log_spruce")
        val leaves = block("leaves_spruce")
        val h = surfaceHeights[jBand] - 1
        val treeH = rng.nextInt(7, 9)
        val canopyTop = max(0, h - treeH)

        val top = canopyTop - 1
        if (top >= 0) setBoth(jBand, top, leaves, foreBand, backBand, bandW)

        for (jj in (jBand - 1)..(jBand + 1)) {
            if (jj in 0 until bandW) setBoth(jj, canopyTop, leaves, foreBand, backBand, bandW)
        }

        for (ring in 1..2) {
            for (jj in (jBand - ring)..(jBand + ring)) {
                if (jj in 0 until bandW) setBoth(jj, canopyTop + 1 + ring, leaves, foreBand, backBand, bandW)
            }
        }

        for (y in h downTo canopyTop) {
            if (jBand in 0 until bandW) backBand[jBand][y] = log
        }
    }

    private fun setBoth(
        jBand: Int,
        y: Int,
        value: Block,
        foreBand: Array<Array<Block>>,
        backBand: Array<Array<Block>>,
        bandW: Int,
    ) {
        if (jBand !in 0 until bandW || y !in 0 until height) return
        foreBand[jBand][y] = value
        backBand[jBand][y] = value
    }

    private fun applySugarCane(jBand: Int, surfaceHeight: Int, foreBand: Array<Array<Block>>, bandW: Int, rng: Random) {
        val supportKeys = setOf("dirt", "grass", "grass_snowed", "sand")
        val support = foreBand[jBand][surfaceHeight]
        if (support.params.key !in supportKeys) return

        val leftWater = jBand > 0 && foreBand[jBand - 1][surfaceHeight].isWater()
        val rightWater = jBand < bandW - 1 && foreBand[jBand + 1][surfaceHeight].isWater()
        if (!leftWater && !rightWater) return

        val topY = surfaceHeight - 1
        if (topY < 0 || !foreBand[jBand][topY].isNone()) return
        if (rng.nextInt(100) >= 25) return

        val caneHeight = rng.nextInt(1, 4)
        for (dy in 0 until caneHeight) {
            val ty = topY - dy
            if (ty < 0 || !foreBand[jBand][ty].isNone()) break
            foreBand[jBand][ty] = sugarCane
        }
    }

    private fun cleanupColumn(surfaceHeight: Int, fore: Array<Block>) {
        val above = surfaceHeight - 1
        if (above < 0) return
        if (fore[above].params.requiresBlock && !fore[surfaceHeight].params.hasCollision) {
            fore[above] = fallback
        }
    }

    companion object {
        const val CHUNK_W = 16

        /** Max horizontal reach of any cross-chunk feature (ore vein width / tree canopy). */
        private const val OVERSCAN = 4

        private const val HEIGHT_SCALE = 0.01
        private const val HEIGHT_OCTAVES = 4
        private const val HEIGHT_PERSISTENCE = 0.5
        private const val HEIGHT_LACUNARITY = 2.0
        private const val HEIGHT_NOISE_Y = 0.5

        private const val CAVE_SCALE_X = 0.08
        private const val CAVE_SCALE_Y = 0.12
        private const val CAVE_THRESHOLD = 0.62
        private const val SURFACE_CAVE_BUFFER = 4

        private const val MIX_A = -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
        private const val MIX_B = -0x3d4d51c2d82b14b1L // 0xC2B2AE3D27D4EB4F
        private const val MIX_C = -0x7ee3623a03d3c8d9L // 0x811C9DC5_... scramble constant

        private const val BIOME_SALT = 0x1111_1111L
        private const val TERRAIN_SALT = 0x2222_2222L
        private const val ORE_SALT = 0x3333_3333L
        private const val CAVE_SPECKLE_SALT = 0x4444_4444L
        private const val FEATURE_SALT = 0x5555_5555L
        private const val CAVE_NOISE_SALT = 0x6666_6666L
    }
}
