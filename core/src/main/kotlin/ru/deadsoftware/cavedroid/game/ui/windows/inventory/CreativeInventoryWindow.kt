package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

class CreativeInventoryWindow() : AbstractInventoryWindow() {

    override val type = GameUiWindow.CREATIVE_INVENTORY

    override var selectedItem: InventoryItem? = null

    fun getMaxScroll(itemsRepository: ItemsRepository): Int {
        return itemsRepository.getAllItems().size / GameWindowsConfigs.Creative.itemsInRow
    }
}