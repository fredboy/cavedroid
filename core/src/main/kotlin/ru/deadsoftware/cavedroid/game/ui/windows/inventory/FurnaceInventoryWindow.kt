package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.game.controller.container.model.Furnace

class FurnaceInventoryWindow(
    val furnace: Furnace,
) : AbstractInventoryWindow() {

    override val type = GameUiWindow.FURNACE

    override var selectedItem: InventoryItem? = null

}