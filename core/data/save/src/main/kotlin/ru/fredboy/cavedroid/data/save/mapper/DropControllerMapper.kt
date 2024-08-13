package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.drop.impl.DropControllerImpl
import javax.inject.Inject

@Reusable
class DropControllerMapper @Inject constructor(
    private val dropMapper: DropMapper
) {

    fun mapSaveData(dropController: DropController): SaveDataDto.DropControllerSaveDataDto {
        return SaveDataDto.DropControllerSaveDataDto(
            version = SAVE_DATA_VERSION,
            drops = dropController.getAllDrop().map(dropMapper::mapSaveData)
        )
    }

    fun mapDropController(saveDataDto: SaveDataDto.DropControllerSaveDataDto): DropController {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return DropControllerImpl(
            initialDrop = saveDataDto.drops.map(dropMapper::mapDrop)
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }

}