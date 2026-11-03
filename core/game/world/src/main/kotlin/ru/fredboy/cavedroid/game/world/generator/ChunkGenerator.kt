package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome

/** Coordinate-based generation; middleware runs on an aligned band before slicing the requested chunk. */
class ChunkGenerator(
    private val config: WorldGeneratorConfig,
    private val itemsRepository: ItemsRepository,
    private val pipeline: WorldGenerationPipeline = WorldGenerationPipeline.full(),
) {
    private val basic = InfiniteBasicTerrainGenerator(config, itemsRepository)

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

    data class GeneratedBand(
        val foreBand: Array<Array<Block>>,
        val backBand: Array<Array<Block>>,
        val bandStart: Int,
    ) {
        override fun equals(other: Any?): Boolean = other is GeneratedBand &&
            bandStart == other.bandStart && foreBand.contentDeepEquals(other.foreBand) && backBand.contentDeepEquals(other.backBand)

        override fun hashCode(): Int = 31 * (31 * foreBand.contentDeepHashCode() + backBand.contentDeepHashCode()) + bandStart
    }

    fun surfaceHeight(x: Int): Int = basic.surfaceHeight(x)

    fun biomeAt(x: Int): Biome = basic.biomeAt(x)

    fun generateChunk(chunkX: Int): GeneratedChunk {
        val band = generateBand(chunkX)
        return GeneratedChunk(
            Array(CHUNK_W) { band.foreBand[OVERSCAN + it] },
            Array(CHUNK_W) { band.backBand[OVERSCAN + it] },
            Array(CHUNK_W) { biomeAt(chunkX * CHUNK_W + it) },
        )
    }

    /** Includes overscan so tests and neighboring chunks can verify shared columns. */
    fun generateBand(chunkX: Int): GeneratedBand {
        val requestedStart = chunkX * CHUNK_W - OVERSCAN
        val requestedWidth = CHUNK_W + 2 * OVERSCAN
        val alignment = pipeline.regionAlignment
        val reach = maxOf(OVERSCAN, pipeline.horizontalReach)
        val bandStart = Math.floorDiv(requestedStart, alignment) * alignment - reach
        val bandEnd = (Math.floorDiv(requestedStart + requestedWidth - 1, alignment) + 1) * alignment + reach
        val context = WorldGenerationContext(config, itemsRepository, bandStart, bandEnd - bandStart)
        pipeline.generate(context) { basic.generate(context) }
        val offset = requestedStart - bandStart
        return GeneratedBand(
            Array(requestedWidth) { context.foreMap[offset + it] },
            Array(requestedWidth) { context.backMap[offset + it] },
            requestedStart,
        )
    }

    companion object {
        const val CHUNK_W = 16
        const val OVERSCAN = 4
    }
}
