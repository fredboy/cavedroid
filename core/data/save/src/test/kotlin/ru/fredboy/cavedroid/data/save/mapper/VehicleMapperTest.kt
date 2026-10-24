package ru.fredboy.cavedroid.data.save.mapper

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.entity.vehicle.VehicleState

@OptIn(ExperimentalSerializationApi::class)
class VehicleMapperTest {
    @Test
    fun `vehicle id health facing position and velocity survive protobuf`() {
        val boat = VehicleState("persistent-id", x = -32.5f, y = 80.25f, velocityX = -5f, velocityY = 0.25f, facing = -1, health = 4)
        val bytes = ProtoBuf.encodeToByteArray(boat.toSaveData())
        assertEquals(boat, ProtoBuf.decodeFromByteArray<SaveDataDto.VehicleSaveDataDto>(bytes).toVehicleState())
    }

    @Test
    fun `legacy chunk bytes with only version restore no vehicles`() {
        val legacy = SaveDataDto.ChunkEntitiesSaveDataDto(1, emptyList(), emptyList(), emptyMap())
        val restored = ProtoBuf.decodeFromByteArray<SaveDataDto.ChunkEntitiesSaveDataDto>(ProtoBuf.encodeToByteArray(legacy))
        assertEquals(emptyList<SaveDataDto.VehicleSaveDataDto>(), restored.vehicles)
    }

    @Test
    fun `chunk vehicles round trip exactly once`() {
        val boat = VehicleState("boat", x = 31.9f, y = 20f).toSaveData()
        val dto = SaveDataDto.ChunkEntitiesSaveDataDto(1, emptyList(), emptyList(), emptyMap(), listOf(boat))
        assertEquals(dto, ProtoBuf.decodeFromByteArray<SaveDataDto.ChunkEntitiesSaveDataDto>(ProtoBuf.encodeToByteArray(dto)))
    }
}
