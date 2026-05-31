package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome

/**
 * Persisted block data for a single infinite-world chunk (a column slice). Only chunks the player
 * has modified are stored; pristine chunks are regenerated from the world seed.
 */
class ChunkSaveData(
    val foreMap: Array<Array<Block>>,
    val backMap: Array<Array<Block>>,
    val biomes: Array<Biome>,
)
