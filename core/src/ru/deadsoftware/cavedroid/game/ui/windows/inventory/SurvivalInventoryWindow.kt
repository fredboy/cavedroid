package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

class SurvivalInventoryWindow(
    gameItemsHolder: GameItemsHolder
) : AbstractInventoryWindowWithCraftGrid(gameItemsHolder) {

    override val type = GameUiWindow.SURVIVAL_INVENTORY

    override var selectedItem: InventoryItem? = null
}