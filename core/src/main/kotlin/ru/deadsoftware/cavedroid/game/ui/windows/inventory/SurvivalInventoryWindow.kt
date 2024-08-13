package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

class SurvivalInventoryWindow(
    itemsRepository: ItemsRepository
) : AbstractInventoryWindowWithCraftGrid(itemsRepository) {

    override val type = GameUiWindow.SURVIVAL_INVENTORY

    override var selectedItem: InventoryItem? = null
}