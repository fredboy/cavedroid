package ru.fredboy.cavedroid.data.save.mapper

import com.badlogic.gdx.Gdx
import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.entity.mob.model.Direction
import javax.inject.Inject

@Reusable
class DirectionMapper @Inject constructor() {

    fun mapSaveData(direction: Direction): SaveDataDto.DirectionSaveDataDto {
        return SaveDataDto.DirectionSaveDataDto(
            version = SAVE_DATA_VERSION,
            value = when (direction) {
                Direction.LEFT -> 0
                Direction.RIGHT -> 1
            },
        )
    }

    fun mapDirection(saveDataDto: SaveDataDto.DirectionSaveDataDto): Direction {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return when (saveDataDto.value) {
            0 -> Direction.LEFT
            1 -> Direction.RIGHT
            else -> {
                Gdx.app.error(TAG, "Unknown direction value: ${saveDataDto.value}")
                Direction.RIGHT
            }
        }
    }

    companion object {
        private const val TAG = "DirectionMapper"
        private const val SAVE_DATA_VERSION = 3
    }
}
