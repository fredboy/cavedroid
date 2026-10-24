package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.save.model.TntEntry
import javax.inject.Inject

@Reusable
class TntMapper @Inject constructor() {
    fun mapSaveData(entries: List<TntEntry>) = SaveDataDto.TntSaveDataDto(
        version = 1,
        entries = entries.map { SaveDataDto.TntEntryDto(1, it.x, it.y, it.velocityX, it.velocityY, it.fuse) },
    )

    fun mapEntries(data: SaveDataDto.TntSaveDataDto): List<TntEntry> {
        data.verifyVersion(1)
        return data.entries.map {
            it.verifyVersion(1)
            TntEntry(it.x, it.y, it.velocityX, it.velocityY, it.fuse)
        }
    }
}
