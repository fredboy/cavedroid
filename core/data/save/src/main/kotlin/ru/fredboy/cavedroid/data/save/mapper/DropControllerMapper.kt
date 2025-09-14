package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.assets.repository.DropSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.drop.DropController
import javax.inject.Inject

@Reusable
class DropControllerMapper @Inject constructor(
    private val dropMapper: DropMapper,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val dropSoundAssetsRepository: DropSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) {

    fun mapSaveData(dropController: DropController): SaveDataDto.DropControllerSaveDataDto = SaveDataDto.DropControllerSaveDataDto(
        version = SAVE_DATA_VERSION,
        drops = dropController.getAllDrop().map(dropMapper::mapSaveData),
    )

    fun mapDropController(
        saveDataDto: SaveDataDto.DropControllerSaveDataDto,
        dropWorldAdapter: DropWorldAdapter,
        dropQueue: DropQueue,
        playerAdapter: PlayerAdapter,
    ): DropController {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return DropController(
            initialDrop = saveDataDto.drops.map { drop ->
                dropMapper.mapDrop(drop, dropWorldAdapter)
            },
            dropWorldAdapter = dropWorldAdapter,
            dropQueue = dropQueue,
            getItemByKeyUseCase = getItemByKeyUseCase,
            playerAdapter = playerAdapter,
            dropSoundAssetsRepository = dropSoundAssetsRepository,
            soundPlayer = soundPlayer,
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 4
    }
}
