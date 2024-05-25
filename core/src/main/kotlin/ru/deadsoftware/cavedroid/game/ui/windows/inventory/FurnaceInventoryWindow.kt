package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.objects.container.Furnace

class FurnaceInventoryWindow(
    val furnace: Furnace,
) : AbstractInventoryWindow() {

    override val type = GameUiWindow.FURNACE

    override var selectedItem: InventoryItem? = null

}