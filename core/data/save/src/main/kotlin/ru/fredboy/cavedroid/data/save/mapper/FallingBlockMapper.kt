package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.model.FallingBlock
import javax.inject.Inject

@Reusable
class FallingBlockMapper @Inject constructor(
    private val directionMapper: DirectionMapper,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) {

    fun mapSaveData(fallingBlock: FallingBlock): SaveDataDto.FallingBlockSaveDataDto {
        return SaveDataDto.FallingBlockSaveDataDto(
            version = SAVE_DATA_VERSION,
            x = fallingBlock.x,
            y = fallingBlock.y,
            width = fallingBlock.width,
            height = fallingBlock.height,
            velocityX =  fallingBlock.velocity.x,
            velocityY = fallingBlock.velocity.y,
            animDelta = fallingBlock.animDelta,
            anim = fallingBlock.anim,
            direction = directionMapper.mapSaveData(fallingBlock.direction),
            dead = fallingBlock.isDead,
            canJump = fallingBlock.canJump,
            flyMode = fallingBlock.isFlyMode,
            maxHealth = fallingBlock.maxHealth,
            health = fallingBlock.health,
            blockKey = fallingBlock.block.params.key,
        )
    }

    fun mapFallingBlock(saveDataDto: SaveDataDto.FallingBlockSaveDataDto): FallingBlock {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return FallingBlock(
            block = getBlockByKeyUseCase[saveDataDto.blockKey],
            x = saveDataDto.x,
            y = saveDataDto.y
        ).apply {
            width = saveDataDto.width
            height = saveDataDto.height
            velocity.x = saveDataDto.velocityX
            velocity.x = saveDataDto.velocityY
            animDelta = saveDataDto.animDelta
            anim = saveDataDto.anim
            direction = directionMapper.mapDirection(saveDataDto.direction)
            canJump = saveDataDto.canJump
            isFlyMode = saveDataDto.flyMode
            health = saveDataDto.health
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}