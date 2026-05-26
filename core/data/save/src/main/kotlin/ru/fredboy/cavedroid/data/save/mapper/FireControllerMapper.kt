package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.save.model.FireEntry
import ru.fredboy.cavedroid.domain.world.model.Layer
import javax.inject.Inject

@Reusable
class FireControllerMapper @Inject constructor() {

    fun mapSaveData(entries: List<FireEntry>): SaveDataDto.FireControllerSaveDataDto = SaveDataDto.FireControllerSaveDataDto(
        version = SAVE_DATA_VERSION,
        entries = entries.map { entry ->
            SaveDataDto.FireEntryDto(
                version = SAVE_DATA_VERSION,
                x = entry.x,
                y = entry.y,
                age = entry.age,
                layer = entry.layer.ordinal,
            )
        },
    )

    fun mapEntries(saveDataDto: SaveDataDto.FireControllerSaveDataDto): List<FireEntry> {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)
        return saveDataDto.entries.map { dto ->
            FireEntry(
                x = dto.x,
                y = dto.y,
                layer = Layer.entries.getOrNull(dto.layer) ?: Layer.FOREGROUND,
                age = dto.age,
            )
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
    }
}
