package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

class CraftingInventoryWindow : AbstractInventoryWindow() {

    override val type = GameUiWindow.CRAFTING_TABLE

    override var selectedItem: InventoryItem? = null

    val craftingItems = MutableList<InventoryItem?>(9) { null }

    var craftResult: InventoryItem? = null
}