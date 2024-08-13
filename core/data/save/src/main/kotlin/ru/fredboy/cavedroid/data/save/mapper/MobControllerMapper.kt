package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.game.controller.mob.impl.MobControllerImpl
import ru.fredboy.cavedroid.game.controller.mob.model.FallingBlock
import ru.fredboy.cavedroid.game.controller.mob.model.Pig
import javax.inject.Inject

@Reusable
class MobControllerMapper @Inject constructor(
    private val pigMapper: PigMapper,
    private val fallingBlockMapper: FallingBlockMapper,
    private val playerMapper: PlayerMapper,
    private val mobAssetsRepository: MobAssetsRepository,
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
) {

    fun mapSaveData(mobController: MobControllerImpl): SaveDataDto.MobControllerSaveDataDto {
        return SaveDataDto.MobControllerSaveDataDto(
            version = SAVE_DATA_VERSION,
            mobs = mobController.mobs.mapNotNull { mob ->
                when (mob) {
                    is Pig -> pigMapper.mapSaveData(mob)
                    is FallingBlock -> fallingBlockMapper.mapSaveData(mob)
                    else -> null
                }
            },
            player = playerMapper.mapSaveData(mobController.player)
        )
    }

    fun mapMobController(saveDataDto: SaveDataDto.MobControllerSaveDataDto): MobControllerImpl {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return MobControllerImpl(
            mobAssetsRepository = mobAssetsRepository,
            getFallbackItemUseCase = getFallbackItemUseCase
        ).apply {
            (mobs as MutableList).addAll(saveDataDto.mobs.mapNotNull { mob ->
                when (mob) {
                    is SaveDataDto.PigSaveDataDto -> pigMapper.mapPig(mob)
                    is SaveDataDto.FallingBlockSaveDataDto -> fallingBlockMapper.mapFallingBlock(mob)
                    else -> null
                }
            })

            player = playerMapper.mapPlayer(saveDataDto.player)
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}