package ru.fredboy.cavedroid.data.save.mapper

import dagger.Reusable
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import javax.inject.Inject

@Reusable
class InventoryItemMapper @Inject constructor(
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) {

    fun mapSaveData(inventoryItem: InventoryItem): SaveDataDto.InventoryItemSaveDataDto {
        return SaveDataDto.InventoryItemSaveDataDto(
            version = SAVE_DATA_VERSION,
            itemKey = inventoryItem.item.params.key,
            amount = inventoryItem.amount
        )
    }

    fun mapInventoryItem(saveDataDto: SaveDataDto.InventoryItemSaveDataDto): InventoryItem {
        saveDataDto.verifyVersion(SAVE_DATA_VERSION)

        return InventoryItem(
            item = getItemByKeyUseCase[saveDataDto.itemKey],
            _amount = saveDataDto.amount,
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 3
    }
}