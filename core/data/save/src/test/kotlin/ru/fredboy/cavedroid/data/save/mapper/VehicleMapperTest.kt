package ru.fredboy.cavedroid.data.save.mapper

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.entity.vehicle.BoatType
import ru.fredboy.cavedroid.entity.vehicle.VehicleState

@OptIn(ExperimentalSerializationApi::class)
class VehicleMapperTest {
    @ParameterizedTest
    @EnumSource(BoatType::class)
    fun `vehicle type id health facing position and velocity survive protobuf`(type: BoatType) {
        val boat = VehicleState("persistent-id", type = type.itemKey, x = -32.5f, y = 80.25f, velocityX = -5f, velocityY = 0.25f, facing = -1, health = 4)
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
        val boats = BoatType.entries.map { VehicleState(it.itemKey, type = it.itemKey, x = 31.9f, y = 20f).toSaveData() }
        val dto = SaveDataDto.ChunkEntitiesSaveDataDto(1, emptyList(), emptyList(), emptyMap(), boats)
        assertEquals(dto, ProtoBuf.decodeFromByteArray<SaveDataDto.ChunkEntitiesSaveDataDto>(ProtoBuf.encodeToByteArray(dto)))
    }
}
