package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.world.WaterFreezing
import kotlin.random.Random

/** Independent streams: never retain a mutable RNG between world or chunk requests. */
class GenerationRandom(private val seed: Long, private val salt: Long = 0) {
    fun stream(additionalSalt: Long = 0): Random = Random(seed xor salt xor additionalSalt)

    fun column(x: Int, additionalSalt: Long): Random {
        var h = seed xor salt xor additionalSalt
        h = h * -0x61c8864680b583ebL + x.toLong() * -0x3d4d51c2d82b14b1L
        h = h xor (h ushr 29)
        h *= -0x7ee3623a03d3c8d9L
        h = h xor (h ushr 32)
        return Random(h)
    }

    fun region(region: Int): Random = Random(seed xor (region.toLong() * 341873128712L) xor salt)
}

class WorldGenerationContext(
    val config: WorldGeneratorConfig,
    val items: ItemsRepository,
    val startX: Int,
    width: Int,
    val loopingWidth: Int? = null,
) {
    val foreMap: Array<Array<Block>> = Array(width) { Array(config.height) { items.fallbackBlock } }
    val backMap: Array<Array<Block>> = Array(width) { Array(config.height) { items.fallbackBlock } }
    val biomes: Array<Biome> = Array(width) { Biome.PLAINS }
    internal lateinit var generateBasic: (GenerationRandom) -> Unit
    lateinit var surfaceHeight: (Int) -> Int
    lateinit var biomeAt: (Int) -> Biome
}

/** Ordered, in-place generation pass. Salts and spatial requirements are part of seed compatibility. */
interface WorldGenerationMiddleware {
    val id: String
    val salt: Long get() = 0
    val regionAlignment: Int get() = 1
    val horizontalReach: Int get() = 0

    fun generate(context: WorldGenerationContext, random: GenerationRandom)
}

/** Basic generation always runs first; additional middleware runs in the supplied order. */
class WorldGenerationPipeline(middleware: List<WorldGenerationMiddleware>) {
    private val middleware = listOf(BasicTerrainMiddleware) + middleware.toList()
    val regionAlignment: Int
    val horizontalReach: Int

    init {
        require(this.middleware.map { it.id }.distinct().size == this.middleware.size) { "Duplicate generation middleware id" }
        require(middleware.all { it.regionAlignment > 0 && it.horizontalReach >= 0 }) { "Invalid middleware spatial requirements" }
        // Preserve the historical basic terrain band, even when structures are disabled.
        regionAlignment = middleware.fold(MonsterRoomGenerator.REGION_WIDTH) { alignment, stage ->
            fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
            val combined = alignment.toLong() / gcd(alignment, stage.regionAlignment) * stage.regionAlignment
            require(combined <= Int.MAX_VALUE) { "Middleware alignment overflow" }
            combined.toInt()
        }
        horizontalReach = maxOf(ChunkGenerator.OVERSCAN, middleware.maxOfOrNull { it.horizontalReach } ?: 0)
    }

    fun generate(context: WorldGenerationContext, basic: (GenerationRandom) -> Unit) {
        context.generateBasic = basic
        middleware.forEach { it.generate(context, GenerationRandom(context.config.seed, it.salt)) }
    }

    companion object {
        fun full(): WorldGenerationPipeline = WorldGenerationPipeline(
            listOf(MineshaftMiddleware, MonsterRoomMiddleware, VillageMiddleware, SurfaceFreezingMiddleware),
        )

        fun basic(): WorldGenerationPipeline = WorldGenerationPipeline(listOf(SurfaceFreezingMiddleware))
    }
}

object BasicTerrainMiddleware : WorldGenerationMiddleware {
    override val id = "basic"

    override fun generate(context: WorldGenerationContext, random: GenerationRandom) = context.generateBasic(random)
}

object MineshaftMiddleware : WorldGenerationMiddleware {
    override val id = "mineshafts"
    override val salt = 0x4D494E45L

    override fun generate(context: WorldGenerationContext, random: GenerationRandom) = with(context) {
        MineshaftGenerator(config, items, random).generate(startX, foreMap, backMap, loopingWidth)
    }
}

object MonsterRoomMiddleware : WorldGenerationMiddleware {
    override val id = "monster_rooms"
    override val salt = 0x44554E47454F4EL
    override val regionAlignment = MonsterRoomGenerator.REGION_WIDTH

    override fun generate(context: WorldGenerationContext, random: GenerationRandom) = with(context) {
        MonsterRoomGenerator(config, items, random).generate(startX, foreMap, backMap, loopingWidth)
    }
}

object VillageMiddleware : WorldGenerationMiddleware {
    override val id = "villages"
    override val salt = 0x56494C4C414745L

    override fun generate(context: WorldGenerationContext, random: GenerationRandom) = with(context) {
        VillageGenerator(config, items, random).generate(startX, foreMap, backMap, surfaceHeight, biomeAt, loopingWidth)
    }
}

object SurfaceFreezingMiddleware : WorldGenerationMiddleware {
    override val id = "surface_freezing"

    override fun generate(context: WorldGenerationContext, random: GenerationRandom) = with(context) {
        val ice = items.getBlockByKey("ice")
        foreMap.indices.forEach { WaterFreezing.freezeSurface(foreMap[it], biomes[it], config.maxSurfaceHeight, ice) }
    }
}
