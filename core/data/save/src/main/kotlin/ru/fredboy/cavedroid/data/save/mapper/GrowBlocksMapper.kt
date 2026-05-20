package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.save.model.GrowBlockEntry
import javax.inject.Inject

@Reusable
class GrowBlocksMapper @Inject constructor() {

    fun mapSaveData(entries: List<GrowBlockEntry>): SaveDataDto.GrowBlocksSaveDataDto = SaveDataDto.GrowBlocksSaveDataDto(
        version = SAVE_DATA_VERSION,
        entries = entries.map { entry ->
            SaveDataDto.GrowBlockEntryDto(
                version = SAVE_DATA_VERSION,
                x = entry.x,
                y = entry.y,
                key = entry.key,
                remainingTicks = entry.remainingTicks,
            )
        },
    )

    fun mapEntries(saveDataDto: SaveDataDto.GrowBlocksSaveDataDto): List<GrowBlockEntry> {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)
        return saveDataDto.entries.map { dto ->
            GrowBlockEntry(
                x = dto.x,
                y = dto.y,
                key = dto.key,
                remainingTicks = dto.remainingTicks,
            )
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
    }
}
