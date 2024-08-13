package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.assets.usecase.GetPlayerSpritesUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.game.controller.mob.model.Player
import javax.inject.Inject

@Reusable
class PlayerMapper @Inject constructor(
    private val directionMapper: DirectionMapper,
    private val inventoryMapper: InventoryMapper,
    private val controlModeMapper: ControlModeMapper,
    private val getPlayerSpritesUseCase: GetPlayerSpritesUseCase,
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
) {

    fun mapSaveData(player: Player): SaveDataDto.PlayerSaveDataDto {
        return SaveDataDto.PlayerSaveDataDto(
            version = SAVE_DATA_VERSION,
            x = player.x,
            y = player.y,
            width = player.width,
            height = player.height,
            velocityX =  player.velocity.x,
            velocityY = player.velocity.y,
            animDelta = player.animDelta,
            anim = player.anim,
            direction = directionMapper.mapSaveData(player.direction),
            dead = player.isDead,
            canJump = player.canJump,
            flyMode = player.isFlyMode,
            maxHealth = player.maxHealth,
            health = player.health,
            hitting = player.isHitting,
            hittingWithDamage = player.isHittingWithDamage,
            hitAnim = player.hitAnim,
            hitAnimDelta = player.hitAnimDelta,
            inventory = inventoryMapper.mapSaveData(player.inventory),
            gameMode = player.gameMode,
            swim = player.swim,
            headRotation = player.headRotation,
            blockDamage = player.blockDamage,
            cursorX = player.cursorX,
            cursorY = player.cursorY,
            spawnPointX = 0f,
            spawnPointY = 0f,
            controlMode = controlModeMapper.mapSaveData(player.controlMode)
        )
    }

    fun mapPlayer(saveDataDto: SaveDataDto.PlayerSaveDataDto): Player {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Player(
            sprite = getPlayerSpritesUseCase(),
            getFallbackItem = getFallbackItemUseCase,
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
            isHitting = saveDataDto.hitting
            isHittingWithDamage = saveDataDto.hittingWithDamage
            hitAnim = saveDataDto.hitAnim
            hitAnimDelta = saveDataDto.hitAnimDelta
            inventory = inventoryMapper.mapInventory(saveDataDto.inventory)
            gameMode = saveDataDto.gameMode
            swim = saveDataDto.swim
            headRotation = saveDataDto.headRotation
            blockDamage = saveDataDto.blockDamage
            cursorX = saveDataDto.cursorX
            cursorY = saveDataDto.cursorY
            controlMode = controlModeMapper.mapControlMode(saveDataDto.controlMode)
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}