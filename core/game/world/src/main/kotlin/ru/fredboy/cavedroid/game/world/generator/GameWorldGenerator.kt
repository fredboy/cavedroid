package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome

class GameWorldGenerator(
    private val config: WorldGeneratorConfig,
    private val itemsRepository: ItemsRepository,
    private val pipeline: WorldGenerationPipeline = WorldGenerationPipeline.full(),
) {
    fun generate(): GeneratedWorld {
        val context = WorldGenerationContext(config, itemsRepository, 0, config.width, config.width)
        pipeline.generate(context) { random ->
            LoopingBasicTerrainGenerator(config, itemsRepository, context, random.stream()).generate()
        }
        return GeneratedWorld(context.foreMap, context.backMap, context.biomes)
    }

    fun villageBeds(): List<Pair<Int, Int>> {
        val context = WorldGenerationContext(config, itemsRepository, 0, 0, config.width)
        return LoopingBasicTerrainGenerator(config, itemsRepository, context, GenerationRandom(config.seed).stream()).villageBeds()
    }

    data class GeneratedWorld(
        val foreMap: Array<Array<Block>>,
        val backMap: Array<Array<Block>>,
        val biomes: Array<Biome>,
    )
}
