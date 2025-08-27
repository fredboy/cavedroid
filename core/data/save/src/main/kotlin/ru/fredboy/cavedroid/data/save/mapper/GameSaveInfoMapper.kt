package ru.fredboy.cavedroid.data.save.mapper

import com.badlogic.gdx.files.FileHandle
import dagger.Reusable
import ru.fredboy.cavedroid.common.utils.DateFormatter
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.save.model.GameSaveInfo
import javax.inject.Inject

@Reusable
internal class GameSaveInfoMapper @Inject constructor(
    private val dateFormatter: DateFormatter,
) {

    fun map(
        dto: SaveDataDto.WorldSaveDataDto,
        dir: String,
        expectedVersion: Int,
        screenshotHandle: FileHandle?,
    ): GameSaveInfo {
        return GameSaveInfo(
            version = dto.version,
            name = dto.name,
            directory = dir,
            lastModifiedString = dateFormatter.format(dto.timestamp),
            lastModifiedTimestamp = dto.timestamp,
            gameMode = dto.gameMode,
            isSupported = dto.version == expectedVersion,
            screenshotHandle = screenshotHandle,
        )
    }
}
