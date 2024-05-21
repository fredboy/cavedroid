package ru.deadsoftware.cavedroid.game.objects.container

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.misc.Saveable

class Chest @JvmOverloads constructor(
    gameItemsHolder: GameItemsHolder,
    initialItems: List<InventoryItem>? = null
) : Container(SIZE, gameItemsHolder, initialItems), Saveable {

    override fun update(gameItemsHolder: GameItemsHolder) {
        // no-op
    }

    override fun getSaveData(): SaveDataDto.ChestSaveData {
        return SaveDataDto.ChestSaveData(
            version = SAVE_DATA_VERSION,
            size = size,
            items = items.map(InventoryItem::getSaveData)
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
        private const val SIZE = 27

        fun fromSaveData(saveData: SaveDataDto.ChestSaveData, gameItemsHolder: GameItemsHolder): Chest {
            saveData.verifyVersion(SAVE_DATA_VERSION)

            return Chest(
                gameItemsHolder = gameItemsHolder,
                initialItems = saveData.items.map { item -> InventoryItem.fromSaveData(item, gameItemsHolder) }
            )
        }
    }
}