package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Plains settlements adapted from Minecraft's houses, irrigated farms and meeting points.
 * Plans depend only on the seed and original terrain, never on the loaded slice or its blocks.
 * Each region owns a complete village; stamping clips that plan to the requested columns.
 */
class VillageGenerator(
    private val config: WorldGeneratorConfig,
    private val items: ItemsRepository,
) {
    fun generate(
        startX: Int,
        fore: Array<Array<Block>>,
        back: Array<Array<Block>>,
        surfaceHeight: (Int) -> Int,
        biomeAt: (Int) -> Biome,
        loopingWidth: Int? = null,
    ) {
        if (!config.villagesEnabled || fore.isEmpty()) return
        val endX = startX + fore.size - 1
        for (region in Math.floorDiv(startX, REGION_WIDTH)..Math.floorDiv(endX, REGION_WIDTH)) {
            val plan = plan(region, surfaceHeight, biomeAt, loopingWidth) ?: continue
            stamp(plan, startX, fore, back, surfaceHeight)
        }
    }

    private enum class Kind(val width: Int) {
        HOUSE(9),
        LARGE_HOUSE(11),
        FARM(9),
        WELL(7),
    }

    private data class Plot(val left: Int, val ground: Int, val kind: Kind, val variant: Int) {
        val right get() = left + kind.width - 1
    }

    private data class Plan(val left: Int, val ground: IntArray, val plots: List<Plot>)

    private fun plan(
        region: Int,
        surfaceHeight: (Int) -> Int,
        biomeAt: (Int) -> Biome,
        loopingWidth: Int?,
    ): Plan? {
        val random = Random(config.seed xor (region.toLong() * 341873128712L) xor 0x56494C4C414745L)
        if (random.nextInt(4) == 0) return null
        val kinds = buildList {
            repeat(random.nextInt(3, 6)) { add(if (random.nextBoolean()) Kind.HOUSE else Kind.LARGE_HOUSE) }
            repeat(random.nextInt(1, 3)) { add(Kind.FARM) }
        }.shuffled(random).toMutableList().apply { add(size / 2, Kind.WELL) }
        val width = kinds.sumOf { it.width } + (kinds.size - 1) * GAP + 2 * APPROACH
        val variants = kinds.map { random.nextInt(3) }
        repeat(8) {
            val left = region * REGION_WIDTH + random.nextInt(8, REGION_WIDTH - width - 8)
            val right = left + width - 1
            if (loopingWidth != null && (left < 0 || right >= loopingWidth)) return@repeat
            if ((left..right).any { biomeAt(it) != Biome.PLAINS }) return@repeat
            val natural = IntArray(width) { surfaceHeight(left + it) }
            // Y points down. Leave headroom for roofs and keep all plots above the waterline.
            if (natural.any { it < CLEARANCE + 1 || it >= minOf(config.seaLevel - 1, config.height - 5) }) return@repeat
            if (natural.max() - natural.min() > 14) return@repeat
            var x = left + APPROACH
            val plots = kinds.mapIndexed { index, kind ->
                val heights = (x until x + kind.width).map { natural[it - left] }.sorted()
                val plot = Plot(x, heights[heights.size / 2], kind, variants[index])
                x += kind.width + GAP
                plot
            }
            if (plots.any { plot -> (plot.left..plot.right).any { abs(natural[it - left] - plot.ground) > 3 } }) return@repeat
            val ground = natural.copyOf()
            for (plot in plots) {
                for (column in plot.left..plot.right) ground[column - left] = plot.ground
            }
            val anchors = buildList {
                add(0 to natural.first())
                for (plot in plots) {
                    // Start slopes beyond the doorway so uphill gravel cannot reduce its clearance.
                    val landing = if (plot.kind == Kind.HOUSE || plot.kind == Kind.LARGE_HOUSE) 1 else 0
                    add(plot.left - left - landing to plot.ground)
                    add(plot.right - left + landing to plot.ground)
                }
                add(width - 1 to natural.last())
            }
            for ((a, b) in anchors.zipWithNext()) {
                for (column in a.first..b.first) {
                    val fraction = (column - a.first).toFloat() / (b.first - a.first)
                    ground[column] = (a.second + fraction * (b.second - a.second)).roundToInt()
                }
            }
            if (ground.asList().zipWithNext().any { (a, b) -> abs(a - b) > 1 }) return@repeat
            return Plan(left, ground, plots)
        }
        return null
    }

    private fun stamp(plan: Plan, startX: Int, fore: Array<Array<Block>>, back: Array<Array<Block>>, surfaceHeight: (Int) -> Int) {
        fun put(x: Int, y: Int, key: String, background: Boolean = false) {
            val localX = x - startX
            if (localX !in fore.indices || y !in 1 until config.height - 1) return
            (if (background) back else fore)[localX][y] = items.getBlockByKey(key)
        }

        for (offset in plan.ground.indices) {
            val x = plan.left + offset
            if (x - startX !in fore.indices) continue
            val ground = plan.ground[offset]
            val natural = surfaceHeight(x)
            // Clear trees in both layers, cut into slopes, and support every filled plot/path.
            for (y in minOf(ground, natural) - CLEARANCE until ground) {
                put(x, y, "none")
                put(x, y, "none", background = true)
            }
            for (y in ground..maxOf(ground, natural) + 3) {
                put(x, y, if (y == ground) "gravel" else "dirt")
                put(x, y, "dirt", background = true)
            }
        }

        for (plot in plan.plots) {
            val left = plot.left
            val right = plot.right
            val ground = plot.ground
            val middle = (left + right) / 2
            when (plot.kind) {
                Kind.HOUSE, Kind.LARGE_HOUSE -> {
                    for (x in left..right) {
                        put(x, ground, "cobblestone")
                        for (y in ground - 4 until ground) {
                            put(x, y, "planks_oak", background = true)
                            if (x == left || x == right) put(x, y, "log_oak")
                        }
                        put(x, ground - 5, "planks_oak")
                        val roofRise = minOf(x - left, right - x, 2)
                        for (rise in 0..roofRise) put(x, ground - 5 - rise, "planks_oak")
                        put(x, ground - 6 - roofRise, "oak_slab_bottom")
                    }
                    for ((x, side) in listOf(left to "left", right to "right")) {
                        put(x, ground - 1, "door_wood_bottom_${side}_closed")
                        put(x, ground - 2, "door_wood_top_${side}_closed")
                    }
                    for (x in listOf(left + 2, right - 2)) put(x, ground - 3, "glass", background = true)
                    // Bed update actions require both halves to be resident in the same chunk.
                    val bedX = left + if (Math.floorMod(left + 2, ChunkGenerator.CHUNK_W) == ChunkGenerator.CHUNK_W - 1) 3 else 2
                    put(bedX, ground - 1, "bed_l")
                    put(bedX + 1, ground - 1, "bed_r")
                    put(middle, ground - 3, "torch")
                    put(right - 2, ground - 1, "chest_village")
                    if (plot.kind == Kind.LARGE_HOUSE) {
                        put(right - 3, ground - 1, if (plot.variant == 0) "furnace" else "crafting_table")
                        if (plot.variant == 2) put(right - 3, ground - 2, "bookshelf", background = true)
                    }
                }
                Kind.FARM -> {
                    for (x in left..right) {
                        put(
                            x,
                            ground,
                            when (x) {
                                left, right -> "log_oak"
                                middle -> "water"
                                else -> "farmland_moist"
                            },
                        )
                        if (x != left && x != right && x != middle) {
                            val crop = when (plot.variant) {
                                0 -> "wheat_stage6"
                                1 -> "carrots_stage3"
                                else -> "potatoes_stage3"
                            }
                            put(x, ground - 1, crop)
                        }
                    }
                }
                Kind.WELL -> {
                    for (x in left..right) {
                        put(x, ground, "cobblestone")
                        put(x, ground + 1, "cobblestone")
                        put(x, ground - 5, "oak_slab_bottom")
                    }
                    // A shallow trough keeps the meeting point traversable in a side-on world.
                    for (x in middle - 1..middle + 1) put(x, ground, "water")
                    for (x in listOf(left, right)) {
                        for (y in ground - 4 until ground) put(x, y, "fence_oak", background = true)
                        put(x, ground - 4, "torch")
                    }
                }
            }
        }

        for ((index, plot) in plan.plots.dropLast(1).withIndex()) {
            val x = plot.right + 2
            val ground = plan.ground[x - plan.left]
            for (y in ground - 3 until ground) put(x, y, "fence_oak", background = true)
            put(x, ground - 3, "torch")
            if (index % 2 == 0) put(x + 1, plan.ground[x + 1 - plan.left] - 1, "hay_block", background = true)
        }
    }

    companion object {
        const val REGION_WIDTH = 256
        private const val GAP = 4
        private const val APPROACH = 4
        private const val CLEARANCE = 12
    }
}
