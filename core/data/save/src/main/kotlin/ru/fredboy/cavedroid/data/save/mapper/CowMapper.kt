package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.Cow
import javax.inject.Inject

@Reusable
class CowMapper @Inject constructor(
    private val directionMapper: DirectionMapper,
    private val gameAssetsHolder: GameAssetsHolder,
) {

    fun mapSaveData(cow: Cow): SaveDataDto.CowSaveDataDto = SaveDataDto.CowSaveDataDto(
        version = SAVE_DATA_VERSION,
        x = cow.position.x,
        y = cow.position.y,
        width = cow.width,
        height = cow.height,
        velocityX = cow.velocity.x,
        velocityY = cow.velocity.y,
        animDelta = cow.animDelta,
        anim = cow.anim,
        direction = directionMapper.mapSaveData(cow.direction),
        dead = cow.isDead,
        canJump = cow.canJump,
        flyMode = cow.isFlyMode,
        maxHealth = cow.maxHealth,
        health = cow.health,
    )

    fun mapCow(
        saveDataDto: SaveDataDto.CowSaveDataDto,
        mobPhysicsFactory: MobPhysicsFactory,
    ): Cow {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Cow(
            sprite = gameAssetsHolder.getCowSprites(),
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
        private const val SAVE_DATA_VERSION = 3
    }
}
