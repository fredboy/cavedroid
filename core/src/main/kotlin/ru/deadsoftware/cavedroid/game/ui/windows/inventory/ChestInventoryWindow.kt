package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.container.model.Chest

class ChestInventoryWindow(val chest: Chest) : AbstractInventoryWindow() {

    override val type = GameUiWindow.CHEST

    override var selectedItem: InventoryItem? = null

}