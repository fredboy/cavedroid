package ru.fredboy.cavedroid.data.save.mapper

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.data.save.model.SaveDataDto

@OptIn(ExperimentalSerializationApi::class)
class VillageGenerationSaveTest {
    @Test
    fun `metadata without village field keeps legacy generation`() {
        // ProtoBuf omits default values, including the newly appended village field.
        val legacy = SaveDataDto.WorldSaveDataDto(1, "old", 0L, 0f, 0, GameMode.SURVIVAL)
        val restored = ProtoBuf.decodeFromByteArray<SaveDataDto.WorldSaveDataDto>(ProtoBuf.encodeToByteArray(legacy))
        assertFalse(restored.villagesEnabled)
    }

    @Test
    fun `village generation setting survives saving either world type`() {
        for (type in 0..1) {
            val original = SaveDataDto.WorldSaveDataDto(
                1, "new", 0L, 0f, 0, GameMode.SURVIVAL,
                seed = 1234L, worldType = type, villagesEnabled = true,
            )
            assertEquals(original, ProtoBuf.decodeFromByteArray<SaveDataDto.WorldSaveDataDto>(ProtoBuf.encodeToByteArray(original)))
        }
    }
}
