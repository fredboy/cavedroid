package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.container.model.Furnace

class FurnaceInventoryWindow(
    val furnace: Furnace,
) : AbstractInventoryWindow() {

    override val type = GameUiWindow.FURNACE

    override var selectedItem: InventoryItem? = null

}