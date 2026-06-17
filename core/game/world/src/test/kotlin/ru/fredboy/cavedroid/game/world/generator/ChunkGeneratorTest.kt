package ru.fredboy.cavedroid.game.world.generator

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

class ChunkGeneratorTest {

    private val nonSolidKeys = setOf(
        "none", "water", "lava", "web", "snow", "sugar_cane",
        "dandelion", "rose", "tallgrass", "deadbush",
    )

    private fun config(seed: Long) = WorldGeneratorConfig.getDefault(seed = seed)

    private fun fakeBlock(key: String): Block {
        val params = mockk<CommonBlockParams>()
        every { params.key } returns key
        every { params.hasCollision } returns (key !in nonSolidKeys)
        every { params.requiresBlock } returns false

        val block = mockk<Block>()
        every { block.params } returns params
        every { block.isNone() } returns false
        every { block.isFluid() } returns (key == "water" || key == "lava")
        every { block.isWater() } returns (key == "water")
        return block
    }

    private fun fakeRepository(): ItemsRepository {
        val cache = HashMap<String, Block>()

        val fallbackParams = mockk<CommonBlockParams>()
        every { fallbackParams.key } returns "none"
        every { fallbackParams.hasCollision } returns false
        every { fallbackParams.requiresBlock } returns false

        val fallback = mockk<Block.None>()
        every { fallback.params } returns fallbackParams
        every { fallback.isNone() } returns true
        every { fallback.isFluid() } returns false
        every { fallback.isWater() } returns false

        val repo = mockk<ItemsRepository>()
        every { repo.fallbackBlock } returns fallback
        every { repo.getBlockByKey(any()) } answers {
            val key = firstArg<String>()
            cache.getOrPut(key) { fakeBlock(key) }
        }
        return repo
    }

    @Test
    fun `surfaceHeight is deterministic across instances with the same seed`() {
        val a = ChunkGenerator(config(SEED), fakeRepository())
        val b = ChunkGenerator(config(SEED), fakeRepository())
        for (x in -200..200) {
            assertEquals(a.surfaceHeight(x), b.surfaceHeight(x), "mismatch at x=$x")
        }
    }

    @Test
    fun `surfaceHeight stays within configured bounds`() {
        val cfg = config(SEED)
        val gen = ChunkGenerator(cfg, fakeRepository())
        for (x in -500..500) {
            val h = gen.surfaceHeight(x)
            assertTrue(h in cfg.minSurfaceHeight..cfg.maxSurfaceHeight, "out of bounds at x=$x: $h")
        }
    }

    @Test
    fun `biomeAt is deterministic and only yields configured biomes`() {
        val cfg = config(SEED)
        val a = ChunkGenerator(cfg, fakeRepository())
        val b = ChunkGenerator(cfg, fakeRepository())
        val allowed = cfg.biomes.toSet()
        for (x in -300..300) {
            val biome = a.biomeAt(x)
            assertEquals(biome, b.biomeAt(x), "biome mismatch at x=$x")
            assertTrue(biome in allowed, "unexpected biome $biome at x=$x")
        }
    }

    @Test
    fun `biome is constant within a min-size cell`() {
        val cfg = config(SEED)
        val gen = ChunkGenerator(cfg, fakeRepository())
        val cellStart = 0
        val expected = gen.biomeAt(cellStart)
        for (x in cellStart until cellStart + cfg.minBiomeSize) {
            assertEquals(expected, gen.biomeAt(x), "biome changed mid-cell at x=$x")
        }
    }

    @Test
    fun `generateChunk is reproducible for the same seed`() {
        // Share one repository so block identities match across the two generators.
        val repo = fakeRepository()
        val a = ChunkGenerator(config(SEED), repo)
        val b = ChunkGenerator(config(SEED), repo)
        for (cx in listOf(-7, -1, 0, 3, 42)) {
            assertEquals(a.generateChunk(cx), b.generateChunk(cx), "chunk $cx not reproducible")
        }
    }

    @Test
    @Disabled
    fun `generateChunk produces full-height columns`() {
        val cfg = config(SEED)
        val chunk = ChunkGenerator(cfg, fakeRepository()).generateChunk(0)
        assertEquals(ChunkGenerator.CHUNK_W, chunk.foreMap.size)
        assertEquals(ChunkGenerator.CHUNK_W, chunk.biomes.size)
        chunk.foreMap.forEach { column -> assertEquals(cfg.height, column.size) }
    }

    @Test
    fun `different seeds produce different terrain`() {
        val a = ChunkGenerator(config(SEED), fakeRepository())
        val b = ChunkGenerator(config(SEED + 1), fakeRepository())
        val differs = (0..500).any { a.surfaceHeight(it) != b.surfaceHeight(it) }
        assertTrue(differs, "terrain identical for different seeds")
    }

    companion object {
        private const val SEED = 123456789L
    }
}
