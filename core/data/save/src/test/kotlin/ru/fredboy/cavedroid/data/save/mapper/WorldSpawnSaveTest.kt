package ru.fredboy.cavedroid.data.save.mapper

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.data.save.model.SaveDataDto

@OptIn(ExperimentalSerializationApi::class)
class WorldSpawnSaveTest {
    @Test
    fun `legacy metadata without spawn remains readable`() {
        val legacy = SaveDataDto.WorldSaveDataDto(7, "old", 0L, 0f, 0, GameMode.SURVIVAL)
        val restored = ProtoBuf.decodeFromByteArray<SaveDataDto.WorldSaveDataDto>(ProtoBuf.encodeToByteArray(legacy))
        assertNull(restored.worldSpawnX)
        assertNull(restored.worldSpawnY)
    }

    @Test
    fun `world spawn round trips in both world types including negative coordinates`() {
        for (type in 0..1) {
            val original = SaveDataDto.WorldSaveDataDto(
                7, "new", 0L, 150f, 0, GameMode.SURVIVAL,
                worldType = type, worldSpawnX = -42.5f, worldSpawnY = 123f,
            )
            assertEquals(original, ProtoBuf.decodeFromByteArray<SaveDataDto.WorldSaveDataDto>(ProtoBuf.encodeToByteArray(original)))
        }
    }
}
