package ru.fredboy.cavedroid.data.save.mapper

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.data.save.model.SaveDataDto

@OptIn(ExperimentalSerializationApi::class)
class WaystoneSaveTest {
    @Test
    fun `older metadata without a waystone network remains readable`() {
        val original = SaveDataDto.WorldSaveDataDto(7, "legacy", 0L, 0f, 0, GameMode.SURVIVAL)
        val restored = ProtoBuf.decodeFromByteArray<SaveDataDto.WorldSaveDataDto>(ProtoBuf.encodeToByteArray(original))
        assertTrue(restored.waystones.isEmpty())
    }

    @Test
    fun `both world types preserve names layers and distant negative coordinates`() {
        for (type in listOf(0, 1)) {
            val original = SaveDataDto.WorldSaveDataDto(
                7,
                "network",
                0L,
                0f,
                0,
                GameMode.SURVIVAL,
                worldType = type,
                waystones = listOf(
                    SaveDataDto.WaystoneSaveDataDto(-100000, 64, 0, "Лесной дом"),
                    SaveDataDto.WaystoneSaveDataDto(100000, 180, 1, "Deep Mine"),
                ),
            )
            assertEquals(original, ProtoBuf.decodeFromByteArray<SaveDataDto.WorldSaveDataDto>(ProtoBuf.encodeToByteArray(original)))
        }
    }
}
