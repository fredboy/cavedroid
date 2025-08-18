package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.PassiveMob
import javax.inject.Inject

@Reusable
class PassiveMobMapper @Inject constructor(
    private val directionMapper: DirectionMapper,
    private val mobParamsRepository: MobParamsRepository,
) {

    fun mapSaveData(mob: PassiveMob): SaveDataDto.PassiveMobSaveDataDto = SaveDataDto.PassiveMobSaveDataDto(
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
    )

    fun mapPassiveMob(
        saveDataDto: SaveDataDto.PassiveMobSaveDataDto,
        mobPhysicsFactory: MobPhysicsFactory,
    ): PassiveMob {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return PassiveMob(
            params = requireNotNull(mobParamsRepository.getMobParamsByKey(saveDataDto.key)),
        ).apply {
            spawn(saveDataDto.x, saveDataDto.y, mobPhysicsFactory)
            velocity.x = saveDataDto.velocityX
            velocity.y = saveDataDto.velocityY
            animDelta = saveDataDto.animDelta
            anim = saveDataDto.anim
            direction = directionMapper.mapDirection(saveDataDto.direction)
            isFlyMode = saveDataDto.flyMode
            health = saveDataDto.health
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 4
    }
}
