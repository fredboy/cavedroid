package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.game.controller.container.model.Chest

class ChestInventoryWindow(val chest: Chest) : AbstractInventoryWindow() {

    override val type = GameUiWindow.CHEST

    override var selectedItem: InventoryItem? = null

}