package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.game.controller.mob.model.Inventory
import javax.inject.Inject

@Reusable
class InventoryMapper @Inject constructor(
    private val inventoryItemMapper: InventoryItemMapper,
    private val getFallbackItem: GetFallbackItemUseCase,
) {

    fun mapSaveData(inventory: Inventory): SaveDataDto.InventorySaveDataDto {
        return SaveDataDto.InventorySaveDataDto(
            version = SAVE_DATA_VERSION,
            size = inventory.size,
            hotbarSize = inventory.hotbarSize,
            activeSlot = inventory.activeSlot,
            items = inventory.items.map(inventoryItemMapper::mapSaveData)
        )
    }

    fun mapInventory(saveDataDto: SaveDataDto.InventorySaveDataDto): Inventory {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Inventory(
            size = saveDataDto.size,
            hotbarSize = saveDataDto.hotbarSize,
            fallbackItem = getFallbackItem(),
            initialItems = saveDataDto.items.map(inventoryItemMapper::mapInventoryItem)
        ).apply {
            activeSlot = saveDataDto.activeSlot
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}