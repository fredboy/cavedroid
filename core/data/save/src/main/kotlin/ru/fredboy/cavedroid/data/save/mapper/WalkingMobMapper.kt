package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.ArcherMob
import ru.fredboy.cavedroid.entity.mob.model.SheepMob
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob
import javax.inject.Inject

@Reusable
class WalkingMobMapper @Inject constructor(
    private val directionMapper: DirectionMapper,
    private val mobParamsRepository: MobParamsRepository,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) {

    fun mapSaveData(mob: WalkingMob): SaveDataDto.WalkingMobSaveDataDto = SaveDataDto.WalkingMobSaveDataDto(
        version = SAVE_DATA_VERSION,
        key = mob.params.key,
        x = mob.position.x,
        y = mob.position.y,
        width = mob.width,
        height = mob.height,
        velocityX = mob.velocity.x,
        velocityY = mob.velocity.y,
        animDelta = mob.animDelta,
        anim = mob.anim,
        direction = directionMapper.mapSaveData(mob.direction),
        dead = mob.isDead,
        canJump = mob.canJump,
        flyMode = mob.isFlyMode,
        maxHealth = mob.maxHealth,
        health = mob.health,
        hasFur = (mob as? SheepMob)?.hasFur ?: false,
        breath = mob.breath,
        woolColor = (mob as? SheepMob)?.woolToColor?.first,
    )

    fun mapPassiveMob(
        saveDataDto: SaveDataDto.WalkingMobSaveDataDto,
        mobPhysicsFactory: MobPhysicsFactory,
    ): WalkingMob {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        val params = requireNotNull(mobParamsRepository.getMobParamsByKey(saveDataDto.key))

        val mob = when (params.behaviorType) {
            MobBehaviorType.SHEEP -> SheepMob(params).apply {
                hasFur = saveDataDto.hasFur
                woolToColor = SheepMob.FUR_COLORS_MAP.values.firstOrNull { (wool, _) -> wool == saveDataDto.woolColor }
                    ?: SheepMob.FUR_COLORS_MAP.values.first()
            }

            MobBehaviorType.ARCHER -> ArcherMob(getItemByKeyUseCase, params)
            else -> WalkingMob(params)
        }

        return mob.apply {
            spawn(saveDataDto.x, saveDataDto.y, mobPhysicsFactory)
            velocity.x = saveDataDto.velocityX
            velocity.y = saveDataDto.velocityY
            animDelta = saveDataDto.animDelta
            anim = saveDataDto.anim
            direction = directionMapper.mapDirection(saveDataDto.direction)
            isFlyMode = saveDataDto.flyMode
            health = saveDataDto.health
            breath = saveDataDto.breath ?: params.maxBreath
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 4
    }
}
