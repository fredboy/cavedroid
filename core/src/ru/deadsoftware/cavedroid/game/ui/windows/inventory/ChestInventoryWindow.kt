package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.objects.container.Chest

class ChestInventoryWindow(val chest: Chest) : AbstractInventoryWindow() {

    override val type = GameUiWindow.CHEST

    override var selectedItem: InventoryItem? = null

}