package ru.fredboy.cavedroid.game.world.generator.verification

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.generator.GameWorldGenerator
import ru.fredboy.cavedroid.game.world.generator.WorldGenerationPipeline
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

internal data class SnapshotCase(val id: String, val config: WorldGeneratorConfig, val infinite: Boolean = false)
internal data class TerrainMaps(val fore: Array<Array<Block>>, val back: Array<Array<Block>>, val biomes: Array<Biome>)

internal object SnapshotCases {
    val cases = buildList {
        val config = WorldGeneratorConfig.getDefault(seed = 123456789L)
        add(SnapshotCase("looping-normal", config))
        add(SnapshotCase("looping-large", config.copy(width = 2048)))
        Biome.entries.forEach { add(SnapshotCase("looping-${it.name.lowercase()}", config.copy(biomes = listOf(it)))) }
        add(SnapshotCase("infinite", config, infinite = true))
    }

    fun capture(
        case: SnapshotCase,
        items: ItemsRepository,
        looping: (WorldGeneratorConfig) -> TerrainMaps = { config ->
            GameWorldGenerator(config, items, WorldGenerationPipeline.basic()).generate().let { TerrainMaps(it.foreMap, it.backMap, it.biomes) }
        },
        infinite: (WorldGeneratorConfig) -> (Int) -> TerrainMaps = { config ->
            val generator = ChunkGenerator(config, items, WorldGenerationPipeline.basic())
            val generate: (Int) -> TerrainMaps = { x -> generator.generateChunk(x).let { TerrainMaps(it.foreMap, it.backMap, it.biomes) } }
            generate
        },
    ): TerrainSnapshot {
        val columns = mutableListOf<TerrainColumn>()
        fun append(start: Int, maps: TerrainMaps) {
            maps.fore.indices.forEach { x ->
                columns += TerrainColumn(start + x, maps.biomes[x].name, maps.fore[x].map(TerrainCell::of), maps.back[x].map(TerrainCell::of))
            }
        }
        if (case.infinite) {
            val generate = infinite(case.config)
            for (x in -64..63) append(x * ChunkGenerator.CHUNK_W, generate(x))
        } else {
            append(0, looping(case.config))
        }
        return TerrainSnapshot(TerrainSnapshot.config(case.config), columns)
    }
}
