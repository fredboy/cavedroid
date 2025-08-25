package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.FallingBlock
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@Reusable
class MobControllerMapper @Inject constructor(
    private val walkingMobMapper: WalkingMobMapper,
    private val fallingBlockMapper: FallingBlockMapper,
    private val playerMapper: PlayerMapper,
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
    private val mobParamsRepository: MobParamsRepository,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) {

    fun mapSaveData(mobController: MobController): SaveDataDto.MobControllerSaveDataDto = SaveDataDto.MobControllerSaveDataDto(
        version = SAVE_DATA_VERSION,
        mobs = mobController.mobs.mapNotNull { mob ->
            when (mob) {
                is WalkingMob -> walkingMobMapper.mapSaveData(mob)
                is FallingBlock -> fallingBlockMapper.mapSaveData(mob)
                else -> null
            }
        },
        player = playerMapper.mapSaveData(mobController.player),
    )

    fun mapMobController(
        saveDataDto: SaveDataDto.MobControllerSaveDataDto,
        mobWorldAdapter: MobWorldAdapter,
        mobPhysicsFactory: MobPhysicsFactory,
        dropQueue: DropQueue,
    ): MobController {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return MobController(
            getFallbackItemUseCase = getFallbackItemUseCase,
            mobParamsRepository = mobParamsRepository,
            mobWorldAdapter = mobWorldAdapter,
            mobPhysicsFactory = mobPhysicsFactory,
            dropQueue = dropQueue,
            getItemByKeyUseCase = getItemByKeyUseCase,
        ).apply {
            (mobs as MutableList).addAll(
                saveDataDto.mobs.mapNotNull { mob ->
                    when (mob) {
                        is SaveDataDto.WalkingMobSaveDataDto -> walkingMobMapper.mapPassiveMob(
                            saveDataDto = mob,
                            mobPhysicsFactory = mobPhysicsFactory,
                        )

                        is SaveDataDto.FallingBlockSaveDataDto -> fallingBlockMapper.mapFallingBlock(
                            saveDataDto = mob,
                            mobPhysicsFactory = mobPhysicsFactory,
                        )

                        else -> null
                    }
                },
            )

            player = playerMapper.mapPlayer(
                saveDataDto = saveDataDto.player,
                mobPhysicsFactory = mobPhysicsFactory,
            )
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 4
    }
}
