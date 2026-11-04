package ru.fredboy.cavedroid.game.world.generator.verification

import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import ru.fredboy.cavedroid.data.items.mapper.BlockMapper
import ru.fredboy.cavedroid.data.items.mapper.DropInfoMapper
import ru.fredboy.cavedroid.data.items.model.BlockDto
import ru.fredboy.cavedroid.data.items.repository.canonicalBlockKey
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockTextureUseCase
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

/** Real block types and generation properties, with texture loading disabled at the GL boundary. */
internal class SnapshotItems(definitions: String) : ItemsRepository by mockk<ItemsRepository>() {
    private val json = Json { ignoreUnknownKeys = true }
    private val mapper = BlockMapper(GetBlockTextureUseCase(mockk()), DropInfoMapper())
    private val blocks = json.parseToJsonElement(definitions).jsonObject.getValue("blocks").jsonObject.mapValues { (key, value) ->
        // Drops do not affect generation and require the repository's polymorphic serializers.
        val dto = json.decodeFromJsonElement(BlockDto.serializer(), JsonObject(value.jsonObject - "drop_info"))
        mapper.map(canonicalBlockKey(key), dto.copy(texture = "none"))
    }
    private val canonicalBlocks = blocks.values.associateBy { it.params.key }
    override val fallbackBlock = canonicalBlocks.getValue("none") as Block.None

    override fun getBlockByKey(key: String): Block = canonicalBlocks.getValue(canonicalBlockKey(key))
}
