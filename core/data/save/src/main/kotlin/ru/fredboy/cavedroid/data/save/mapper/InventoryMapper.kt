package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.mob.model.WearingArmor
import javax.inject.Inject

@Reusable
class InventoryMapper @Inject constructor(
    private val inventoryItemMapper: InventoryItemMapper,
    private val getFallbackItem: GetFallbackItemUseCase,
) {

    fun mapSaveData(inventory: Inventory): SaveDataDto.InventorySaveDataDto = SaveDataDto.InventorySaveDataDto(
        version = SAVE_DATA_VERSION,
        size = inventory.size,
        items = inventory.items.map(inventoryItemMapper::mapSaveData),
    )

    fun mapSaveData(wearingArmor: WearingArmor): SaveDataDto.InventorySaveDataDto {
        return SaveDataDto.InventorySaveDataDto(
            version = SAVE_DATA_VERSION,
            size = wearingArmor.items.size,
            items = wearingArmor.items.map(inventoryItemMapper::mapSaveData),
        )
    }

    fun mapInventory(saveDataDto: SaveDataDto.InventorySaveDataDto): Inventory {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return Inventory(
            size = saveDataDto.size,
            fallbackItem = getFallbackItem(),
            initialItems = saveDataDto.items.map(inventoryItemMapper::mapInventoryItem),
        )
    }

    fun mapWearingArmor(saveDataDto: SaveDataDto.InventorySaveDataDto): WearingArmor {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return WearingArmor(getFallbackItem()).apply {
            for (i in items.indices) {
                items[i] = saveDataDto.items.getOrNull(i)?.let(inventoryItemMapper::mapInventoryItem)
                    ?: getFallbackItem().toInventoryItem()
            }
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}
