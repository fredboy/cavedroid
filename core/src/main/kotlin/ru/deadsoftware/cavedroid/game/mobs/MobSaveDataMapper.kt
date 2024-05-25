package ru.deadsoftware.cavedroid.game.mobs

import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto

fun fromSaveData(saveData: SaveDataDto.MobSaveDataDto): Mob {
    return when (saveData) {
        is SaveDataDto.PigSaveData -> Pig.fromSaveData(saveData)
        is SaveDataDto.FallingBlockSaveData -> FallingBlock.fromSaveData(saveData)

        is SaveDataDto.PlayerSaveData -> throw IllegalArgumentException("Cannot load player as regular Mob")
    }
}