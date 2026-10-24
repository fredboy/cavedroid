package ru.fredboy.cavedroid.data.save.mapper

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.save.model.TntEntry

@OptIn(ExperimentalSerializationApi::class)
class TntMapperTest {
    private val mapper = TntMapper()

    @Test
    fun `primed TNT survives protobuf serialization including negative coordinates and partial fuse`() {
        val entries = listOf(TntEntry(-32.5f, 127.51f, -0.3f, 12f, 1.25f), TntEntry(255.5f, 0.5f, 0f, -4f, 4f))
        val bytes = ProtoBuf.encodeToByteArray(mapper.mapSaveData(entries))
        assertEquals(entries, mapper.mapEntries(ProtoBuf.decodeFromByteArray<SaveDataDto.TntSaveDataDto>(bytes)))
    }

    @Test
    fun `empty TNT save replaces an earlier active fuse list`() {
        val bytes = ProtoBuf.encodeToByteArray(mapper.mapSaveData(emptyList()))
        assertEquals(emptyList<TntEntry>(), mapper.mapEntries(ProtoBuf.decodeFromByteArray<SaveDataDto.TntSaveDataDto>(bytes)))
    }
}
