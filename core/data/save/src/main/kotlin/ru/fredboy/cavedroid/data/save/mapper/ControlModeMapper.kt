package ru.fredboy.cavedroid.data.save.mapper

import com.badlogic.gdx.Gdx
import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.entity.mob.model.Player
import javax.inject.Inject

@Reusable
class ControlModeMapper @Inject constructor() {

    fun mapSaveData(direction: Player.ControlMode): SaveDataDto.ControlModeSaveDataDto = SaveDataDto.ControlModeSaveDataDto(
        version = SAVE_DATA_VERSION,
        value = when (direction) {
            Player.ControlMode.WALK -> 0
            Player.ControlMode.CURSOR -> 1
        },
    )

    fun mapControlMode(saveDataDto: SaveDataDto.ControlModeSaveDataDto): Player.ControlMode {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return when (saveDataDto.value) {
            0 -> Player.ControlMode.WALK
            1 -> Player.ControlMode.CURSOR
            else -> {
                logger.e { "Unknown control mode value: ${saveDataDto.value}" }
                Player.ControlMode.CURSOR
            }
        }
    }

    companion object {
        private const val TAG = "ControlModeMapper"
private val logger = co.touchlab.kermit.Logger.withTag(TAG)
        private const val SAVE_DATA_VERSION = 3
    }
}
