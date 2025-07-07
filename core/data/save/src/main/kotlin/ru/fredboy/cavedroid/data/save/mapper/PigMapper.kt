package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.assets.usecase.GetPigSpritesUseCase
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.Pig
import ru.fredboy.cavedroid.game.controller.mob.behavior.PigMobBehavior
import javax.inject.Inject

@Reusable
class PigMapper @Inject constructor(
    private val directionMapper: DirectionMapper,
    private val getPigSpriteUseCase: GetPigSpritesUseCase,
) {

    fun mapSaveData(pig: Pig): SaveDataDto.PigSaveDataDto = SaveDataDto.PigSaveDataDto(
        version = SAVE_DATA_VERSION,
        x = pig.position.x,
        y = pig.position.y,
        width = pig.width,
        height = pig.height,
        velocityX = pig.velocity.x,
        velocityY = pig.velocity.y,
        animDelta = pig.animDelta,
        anim = pig.anim,
        direction = directionMapper.mapSaveData(pig.direction),
        dead = pig.isDead,
        canJump = pig.canJump,
        flyMode = pig.isFlyMode,
        maxHealth = pig.maxHealth,
        health = pig.health,
    )

    fun mapPig(
        saveDataDto: SaveDataDto.PigSaveDataDto,
        mobPhysicsFactory: MobPhysicsFactory,
    ): Pig {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Pig(
            sprite = getPigSpriteUseCase(),
            behavior = PigMobBehavior(),
        ).apply {
            spawn(saveDataDto.x - width / 2f, saveDataDto.y - height / 2f, mobPhysicsFactory)
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
        private const val SAVE_DATA_VERSION = 3
    }
}
