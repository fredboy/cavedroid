package ru.fredboy.cavedroid.data.save.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
sealed class SaveDataDto {

    abstract val version: Int

    fun verifyVersion(expectedVersion: Int) {
        require(version == expectedVersion) {
            "${this::class.simpleName} version mismatch ($version != $expectedVersion)"
        }
    }

    @Serializable
    data class DirectionSaveDataDto(
        override val version: Int,
        val value: Int,
    ) : SaveDataDto()

    @Serializable
    data class ControlModeSaveDataDto(
        override val version: Int,
        val value: Int,
    ) : SaveDataDto()

    @Serializable
    sealed class ContainerSaveDataDto : SaveDataDto() {
        abstract val size: Int
        abstract val items: List<InventoryItemSaveDataDto>
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
        abstract val animDelta: Float
        abstract val anim: Float
        abstract val direction: DirectionSaveDataDto
        abstract val dead: Boolean
        abstract val canJump: Boolean
        abstract val flyMode: Boolean
        abstract val maxHealth: Int
        abstract val health: Int
    }

    @Serializable
    data class InventoryItemSaveDataDto(
        override val version: Int,
        val itemKey: String,
        val amount: Int,
    ) : SaveDataDto()

    @Serializable
    data class InventorySaveDataDto(
        override val version: Int,
        override val size: Int,
        override val items: List<InventoryItemSaveDataDto>,
    ) : ContainerSaveDataDto()

    @Serializable
    data class FurnaceSaveDataDto(
        override val version: Int,
        override val size: Int,
        val currentFuelItemKey: String?,
        override val items: List<InventoryItemSaveDataDto>,
        val startBurnTimeMs: Long,
        val startSmeltTimeMs: Long,
        val burnProgress: Float,
        val smeltProgress: Float,
    ) : ContainerSaveDataDto()

    @Serializable
    data class ChestSaveDataDto(
        override val version: Int,
        override val size: Int,
        override val items: List<InventoryItemSaveDataDto>,
    ) : ContainerSaveDataDto()

    @Serializable
    data class ContainerControllerSaveDataDto(
        override val version: Int,
        val containerMap: Map<String, @Contextual ContainerSaveDataDto>,
    ) : SaveDataDto()

    @Serializable
    data class DropSaveDataDto(
        override val version: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        val itemKey: String,
        val amount: Int,
        val pickedUp: Boolean,
    ) : RectangleObjectSaveDataDto()

    @Serializable
    data class DropControllerSaveDataDto(
        override val version: Int,
        val drops: List<DropSaveDataDto>,
    ) : SaveDataDto()

    @Serializable
    data class PigSaveDataDto(
        override val version: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        override val animDelta: Float,
        override val anim: Float,
        override val direction: DirectionSaveDataDto,
        override val dead: Boolean,
        override val canJump: Boolean,
        override val flyMode: Boolean,
        override val maxHealth: Int,
        override val health: Int,
    ) : MobSaveDataDto()

    @Serializable
    data class CowSaveDataDto(
        override val version: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        override val animDelta: Float,
        override val anim: Float,
        override val direction: DirectionSaveDataDto,
        override val dead: Boolean,
        override val canJump: Boolean,
        override val flyMode: Boolean,
        override val maxHealth: Int,
        override val health: Int,
    ) : MobSaveDataDto()

    @Serializable
    data class FallingBlockSaveDataDto(
        override val version: Int,
        override val x: Float,
        override val y: Float,
        override val width: Float,
        override val height: Float,
        override val velocityX: Float,
        override val velocityY: Float,
        override val animDelta: Float,
        override val anim: Float,
        override val direction: DirectionSaveDataDto,
        override val dead: Boolean,
        override val canJump: Boolean,
        override val flyMode: Boolean,
        override val maxHealth: Int,
        override val health: Int,
        val blockKey: String,
    ) : MobSaveDataDto()

    @Serializable
    data class PlayerSaveDataDto(
        override val version: Int,
        override val animDelta: Float,
        override val anim: Float,
        override val direction: DirectionSaveDataDto,
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
        val inventory: InventorySaveDataDto,
        val gameMode: Int,
        val headRotation: Float,
        val blockDamage: Float,
        val cursorX: Int,
        val cursorY: Int,
        val spawnPointX: Float,
        val spawnPointY: Float,
        val controlMode: ControlModeSaveDataDto,
        val activeSlot: Int,
    ) : MobSaveDataDto()

    @Serializable
    data class MobControllerSaveDataDto(
        override val version: Int,
        val mobs: List<@Contextual MobSaveDataDto>,
        val player: PlayerSaveDataDto,
    ) : SaveDataDto()
}
