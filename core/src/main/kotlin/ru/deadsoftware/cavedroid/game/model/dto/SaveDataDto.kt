package ru.deadsoftware.cavedroid.game.model.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import ru.deadsoftware.cavedroid.game.mobs.Mob
import ru.deadsoftware.cavedroid.game.mobs.player.Player.ControlMode

@Serializable
sealed class SaveDataDto {

    abstract val version: Int

    fun verifyVersion(expectedVersion: Int) {
        require(version == expectedVersion) {
            "${this::class.simpleName} version mismatch ($version != $expectedVersion)"
        }
    }

    @Serializable
    sealed class ContainerSaveDataDto : SaveDataDto() {
        abstract val size: Int
        abstract val items: List<InventoryItemSaveData>
    }

    @Serializable
    sealed class RectangleObjectSaveDataDto : SaveDataDto() {
        abstract val x: Float
        abstract val y: Float
        abstract val width: Float
        abstract val height: Float
        abstract val velocityX: Float
        abstract val velocityY: Float
    }

    @Serializable
    sealed class MobSaveDataDto : RectangleObjectSaveDataDto() {
        abstract val type: Mob.Type
        abstract val animDelta: Int
        abstract val anim: Float
        abstract val direction: Mob.Direction
        abstract val dead: Boolean
        abstract val canJump: Boolean
        abstract val flyMode: Boolean
        abstract val maxHealth: Int
        abstract val health: Int
    }

    @Serializable
    data class InventoryItemSaveData(
        override val version: Int,
        val itemKey: String,
        val amount: Int,
    ) : SaveDataDto()

    @Serializable
    data class InventorySaveData(
        override val version: Int,
        override val size: Int,
        val hotbarSize: Int,
        val activeSlot: Int,
        override val items: List<InventoryItemSaveData>,
    ) : ContainerSaveDataDto()

    @Serializable
    data class FurnaceSaveData(
        override val version: Int,
        override val size: Int,
        val currentFuelItemKey: String?,
        override val items: List<InventoryItemSaveData>,
        val startBurnTimeMs: Long,
        val startSmeltTimeMs: Long,
        val burnProgress: Float,
        val smeltProgress: Float,
    ) : ContainerSaveDataDto()

    @Serializable
    data class ChestSaveData(
        override val version: Int,
        override val size: Int,
        override val items: List<InventoryItemSaveData>
    ) : ContainerSaveDataDto()

    @Serializable
    data class ContainerControllerSaveData(
        override val version: Int,
        val containerMap: Map<String, @Contextual ContainerSaveDataDto>,
    ): SaveDataDto()

    @Serializable
    data class DropSaveData(
        override val version: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        val itemKey: String,
        val amount: Int,
        val pickedUp: Boolean
    ) : RectangleObjectSaveDataDto()
    
    @Serializable
    data class DropControllerSaveData(
        override val version: Int,
        val drops: List<DropSaveData>
    ) : SaveDataDto()
    
    @Serializable
    data class PigSaveData(
        override val version: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        override val type: Mob.Type,
        override val animDelta: Int,
        override val anim: Float,
        override val direction: Mob.Direction,
        override val dead: Boolean,
        override val canJump: Boolean,
        override val flyMode: Boolean,
        override val maxHealth: Int,
        override val health: Int,
    ) : MobSaveDataDto()

    @Serializable
    data class FallingBlockSaveData(
        override val version: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        override val type: Mob.Type,
        override val animDelta: Int,
        override val anim: Float,
        override val direction: Mob.Direction,
        override val dead: Boolean,
        override val canJump: Boolean,
        override val flyMode: Boolean,
        override val maxHealth: Int,
        override val health: Int,
        val blockKey: String,
    ) : MobSaveDataDto()

    @Serializable
    data class PlayerSaveData(
        override val version: Int,
        override val type: Mob.Type,
        override val animDelta: Int,
        override val anim: Float,
        override val direction: Mob.Direction,
        override val dead: Boolean,
        override val canJump: Boolean,
        override val flyMode: Boolean,
        override val maxHealth: Int,
        override val health: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        val hitting: Boolean,
        val hittingWithDamage: Boolean,
        val hitAnim: Float,
        val hitAnimDelta: Float,
        val inventory: InventorySaveData,
        val gameMode: Int,
        val swim: Boolean,
        val headRotation: Float,
        val blockDamage: Float,
        val cursorX: Int,
        val cursorY: Int,
        val spawnPointX: Float,
        val spawnPointY: Float,
        val controlMode: ControlMode,
    ) : MobSaveDataDto()

    @Serializable
    data class MobsControllerSaveData(
        override val version: Int,
        val mobs: List<@Contextual MobSaveDataDto>,
        val player: PlayerSaveData,
    ) : SaveDataDto()
    
}