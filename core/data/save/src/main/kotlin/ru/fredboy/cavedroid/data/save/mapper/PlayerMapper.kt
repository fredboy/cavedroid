package ru.fredboy.cavedroid.data.save.mapper

import com.badlogic.gdx.math.Vector2
import dagger.Reusable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.Player
import javax.inject.Inject

@Reusable
class PlayerMapper @Inject constructor(
    private val directionMapper: DirectionMapper,
    private val inventoryMapper: InventoryMapper,
    private val controlModeMapper: ControlModeMapper,
    private val getFallbackItemUseCase: GetFallbackItemUseCase,
    private val mobParamsRepository: MobParamsRepository,
    private val tooltipManager: TooltipManager,
    private val soundPlayer: SoundPlayer,
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
) {

    fun mapSaveData(player: Player): SaveDataDto.PlayerSaveDataDto = SaveDataDto.PlayerSaveDataDto(
        version = SAVE_DATA_VERSION,
        key = player.params.key,
        x = player.position.x,
        y = player.position.y,
        width = player.width,
        height = player.height,
        velocityX = player.velocity.x,
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
        headRotation = player.headRotation,
        blockDamage = player.blockDamage,
        cursorX = player.selectedX,
        cursorY = player.selectedY,
        spawnPointX = player.spawnPoint?.x ?: 0f,
        spawnPointY = player.spawnPoint?.y ?: 0f,
        controlMode = controlModeMapper.mapSaveData(player.controlMode),
        activeSlot = player.activeSlot,
        breath = player.breath,
        wearingArmor = inventoryMapper.mapSaveData(player.wearingArmor),
    )

    fun mapPlayer(
        saveDataDto: SaveDataDto.PlayerSaveDataDto,
        mobPhysicsFactory: MobPhysicsFactory,
    ): Player {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Player(
            getFallbackItem = getFallbackItemUseCase,
            tooltipManager = tooltipManager,
            params = requireNotNull(mobParamsRepository.getMobParamsByKey(saveDataDto.key)),
            soundPlayer = soundPlayer,
            stepsSoundAssetsRepository = stepsSoundAssetsRepository,
        ).apply {
            spawn(saveDataDto.x, saveDataDto.y, mobPhysicsFactory)
            velocity.x = saveDataDto.velocityX
            velocity.y = saveDataDto.velocityY
            animDelta = saveDataDto.animDelta
            anim = saveDataDto.anim
            direction = directionMapper.mapDirection(saveDataDto.direction)
            isFlyMode = saveDataDto.flyMode
            health = saveDataDto.health
            isHitting = saveDataDto.hitting
            isHittingWithDamage = saveDataDto.hittingWithDamage
            hitAnim = saveDataDto.hitAnim
            hitAnimDelta = saveDataDto.hitAnimDelta
            inventory = inventoryMapper.mapInventory(saveDataDto.inventory)
            gameMode = saveDataDto.gameMode
            headRotation = saveDataDto.headRotation
            blockDamage = saveDataDto.blockDamage
            cursorX = saveDataDto.cursorX.toFloat()
            cursorY = saveDataDto.cursorY.toFloat()
            controlMode = controlModeMapper.mapControlMode(saveDataDto.controlMode)
            spawnPoint = Vector2(saveDataDto.spawnPointX, saveDataDto.spawnPointY)
            activeSlot = saveDataDto.activeSlot
            breath = saveDataDto.breath ?: params.maxBreath
            wearingArmor = saveDataDto.wearingArmor?.let(inventoryMapper::mapWearingArmor) ?: wearingArmor
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 6
    }
}
