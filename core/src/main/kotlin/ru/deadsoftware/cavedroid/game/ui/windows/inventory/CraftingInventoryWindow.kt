package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

class CraftingInventoryWindow(
    itemsRepository: ItemsRepository
) : AbstractInventoryWindowWithCraftGrid(itemsRepository) {

    override val type = GameUiWindow.CRAFTING_TABLE

    override var selectedItem: InventoryItem? = null
}