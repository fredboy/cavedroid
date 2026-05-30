package ru.fredboy.cavedroid.game.world.store

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.generator.GameWorldGenerator
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

/**
 * Classic fixed-width, horizontally-looping world. Backs the whole map with dense arrays and wraps
 * X by modulo. This preserves the historical behaviour exactly; it was extracted verbatim from
 * `GameWorld`.
 */
class FiniteLoopingBlockStore(
    private val itemsRepository: ItemsRepository,
    override val generatorConfig: WorldGeneratorConfig,
    initialForeMap: Array<Array<Block>>?,
    initialBackMap: Array<Array<Block>>?,
    initialBiomes: Array<Biome>?,
) : WorldBlockStore {

    override val width: Int = generatorConfig.width
    override val height: Int = generatorConfig.height
    override val isInfinite: Boolean = false

    override val foreMap: Array<Array<Block>>
    override val backMap: Array<Array<Block>>
    override val biomes: Array<Biome>

    init {
        if (initialForeMap != null && initialBackMap != null) {
            foreMap = initialForeMap
            backMap = initialBackMap
            biomes = initialBiomes ?: Array(width) { Biome.PLAINS }
        } else {
            val generated = GameWorldGenerator(generatorConfig, itemsRepository).generate()
            foreMap = generated.foreMap
            backMap = generated.backMap
            biomes = generated.biomes
        }
    }

    override fun transformX(x: Int): Int {
        var transformed = x % width
        if (transformed < 0) {
            transformed = width + x
        }
        return transformed
    }

    override fun isInBounds(x: Int, y: Int): Boolean {
        return y in 0..<height && transformX(x) in 0..<width
    }

    override fun getBiomeAt(x: Int): Biome = biomes[transformX(x)]

    override fun getBlock(x: Int, y: Int, layer: Layer): Block {
        val fallback = itemsRepository.fallbackBlock

        if (y !in 0..<height) {
            return fallback
        }

        val transformedX = transformX(x)

        if (transformedX !in 0..<width) {
            return fallback
        }

        return when (layer) {
            Layer.FOREGROUND -> foreMap[transformedX][y]
            Layer.BACKGROUND -> backMap[transformedX][y]
        }
    }

    override fun setBlock(x: Int, y: Int, layer: Layer, value: Block) {
        if (y !in 0..<height) {
            return
        }

        val transformedX = transformX(x)

        if (transformedX !in 0..<width) {
            return
        }

        when (layer) {
            Layer.FOREGROUND -> foreMap[transformedX][y] = value
            Layer.BACKGROUND -> backMap[transformedX][y] = value
        }
    }

    override fun dispose() = Unit
}
