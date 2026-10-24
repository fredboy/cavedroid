package ru.fredboy.cavedroid.data.save.mapper

import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.entity.vehicle.VehicleState

fun VehicleState.toSaveData() = SaveDataDto.VehicleSaveDataDto(
    id = id,
    type = type,
    x = x,
    y = y,
    velocityX = velocityX,
    velocityY = velocityY,
    facing = facing,
    health = health,
)

fun SaveDataDto.VehicleSaveDataDto.toVehicleState(): VehicleState {
    verifyVersion(1)
    return VehicleState(id, type, x, y, velocityX, velocityY, facing, health)
}
