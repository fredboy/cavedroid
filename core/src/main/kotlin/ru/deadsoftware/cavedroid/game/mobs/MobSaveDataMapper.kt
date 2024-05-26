package ru.deadsoftware.cavedroid.game.mobs

import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto
import ru.fredboy.cavedroid.domain.assets.usecase.GetPigSpritesUseCase

fun fromSaveData(
    saveData: SaveDataDto.MobSaveDataDto,
    getPigSprites: GetPigSpritesUseCase,
): Mob {
    return when (saveData) {
        is SaveDataDto.PigSaveData -> Pig.fromSaveData(getPigSprites, saveData)
        is SaveDataDto.FallingBlockSaveData -> FallingBlock.fromSaveData(saveData)

        is SaveDataDto.PlayerSaveData -> throw IllegalArgumentException("Cannot load player as regular Mob")
    }
}