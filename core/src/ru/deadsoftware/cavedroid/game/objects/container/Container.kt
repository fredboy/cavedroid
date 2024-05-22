package ru.deadsoftware.cavedroid.game.objects.container

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.misc.Saveable

abstract class Container @JvmOverloads constructor(
    val size: Int,
    gameItemsHolder: GameItemsHolder,
    initialItems: List<InventoryItem>? = null,
) : Saveable {

    private val _items = Array(size) { index ->
        initialItems?.getOrNull(index) ?: gameItemsHolder.fallbackItem.toInventoryItem()
    }

    val items get() = _items.asList() as MutableList<InventoryItem>

    open fun initItems(gameItemsHolder: GameItemsHolder) {
        _items.forEach { it.init(gameItemsHolder) }
    }

    abstract fun update(gameItemsHolder: GameItemsHolder)

    abstract override fun getSaveData(): SaveDataDto.ContainerSaveDataDto

    companion object {
        fun fromSaveData(saveData: SaveDataDto.ContainerSaveDataDto, gameItemsHolder: GameItemsHolder): Container {
            return when (saveData) {
                is SaveDataDto.FurnaceSaveData -> Furnace.fromSaveData(saveData, gameItemsHolder)
                is SaveDataDto.ChestSaveData -> Chest.fromSaveData(saveData, gameItemsHolder)

                is SaveDataDto.InventorySaveData -> {
                    throw IllegalArgumentException("Cannot load Container from InventorySaveData")
                }
            }
        }
    }
}