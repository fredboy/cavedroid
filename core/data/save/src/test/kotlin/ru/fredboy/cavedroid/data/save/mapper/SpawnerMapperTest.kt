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
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.container.model.Spawner

@OptIn(ExperimentalSerializationApi::class)
class SpawnerMapperTest {
    private val items = mockk<ItemsRepository>().also { every { it.fallbackItem } returns mockk() }
    private val mapper = SpawnerMapper(GetFallbackItemUseCase(items))

    @Test
    fun `spawner type and cooldown survive finite and chunk protobuf containers`() {
        for (key in listOf("spider", "cow", "skeleton", "custom_mob")) {
            val dto = mapper.mapSaveData(Spawner(items.fallbackItem, key, 13.25f))
            val finite = SaveDataDto.ContainerControllerSaveDataDto(3, mapOf("(-17;180;0)" to dto))
            val decoded = ProtoBuf.decodeFromByteArray<SaveDataDto.ContainerControllerSaveDataDto>(ProtoBuf.encodeToByteArray(finite))
            val restored = mapper.mapSpawner(decoded.containerMap.values.single() as SaveDataDto.SpawnerSaveDataDto)
            assertEquals(key, restored.mobKey)
            assertEquals(13.25f, restored.remainingSeconds)
            val chunk = SaveDataDto.ChunkEntitiesSaveDataDto(1, emptyList(), emptyList(), finite.containerMap)
            assertEquals(chunk, ProtoBuf.decodeFromByteArray<SaveDataDto.ChunkEntitiesSaveDataDto>(ProtoBuf.encodeToByteArray(chunk)))
        }
    }

    @Test
    fun `existing chest container encoding remains readable`() {
        val old = SaveDataDto.ContainerControllerSaveDataDto(
            3,
            mapOf("(1;2;1)" to SaveDataDto.ChestSaveDataDto(3, 27, emptyList())),
        )
        assertEquals(old, ProtoBuf.decodeFromByteArray<SaveDataDto.ContainerControllerSaveDataDto>(ProtoBuf.encodeToByteArray(old)))
    }
}
