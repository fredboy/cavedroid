package ru.fredboy.cavedroid.data.save.mapper

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.container.model.Chest

@OptIn(ExperimentalSerializationApi::class)
class LargeChestSaveTest {
    @Test
    fun `both halves retain every slot in finite and streamed chunk saves`() {
        val empty = Item.None(mockk { every { key } returns "none" })
        val item = Item.Normal(mockk { every { key } returns "diamond" }, mockk())
        val items = mockk<ItemsRepository> {
            every { fallbackItem } returns empty
            every { getItemByKey("diamond") } returns item
        }
        val mapper = ChestMapper(InventoryItemMapper(GetItemByKeyUseCase(items)), GetFallbackItemUseCase(items))
        val halves = listOf(Chest(empty), Chest(empty))
        halves.forEachIndexed { half, chest ->
            chest.items.indices.forEach { slot -> chest.items[slot] = item.toInventoryItem(half * 27 + slot + 1) }
        }
        val containers = halves.mapIndexed { index, chest -> "($index;4;1)" to mapper.mapSaveData(chest) }.toMap()
        val finite = SaveDataDto.ContainerControllerSaveDataDto(3, containers)
        val restored = ProtoBuf.decodeFromByteArray<SaveDataDto.ContainerControllerSaveDataDto>(ProtoBuf.encodeToByteArray(finite))
        val chunk = SaveDataDto.ChunkEntitiesSaveDataDto(1, emptyList(), emptyList(), containers)
        val restoredChunk = ProtoBuf.decodeFromByteArray<SaveDataDto.ChunkEntitiesSaveDataDto>(ProtoBuf.encodeToByteArray(chunk))
        for (saved in listOf(restored.containerMap, restoredChunk.containers)) {
            halves.forEachIndexed { index, chest ->
                val decoded = mapper.mapChest(saved.getValue("($index;4;1)") as SaveDataDto.ChestSaveDataDto)
                assertEquals(27, decoded.size)
                assertEquals(chest.items.map { it.amount }, decoded.items.map { it.amount })
                assertEquals(chest.items.map { it.item.params.key }, decoded.items.map { it.item.params.key })
            }
        }
    }
}
