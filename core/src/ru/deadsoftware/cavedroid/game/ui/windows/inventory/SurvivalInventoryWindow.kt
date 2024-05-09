package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

class SurvivalInventoryWindow() : AbstractInventoryWindow() {

    override val type = GameUiWindow.SURVIVAL_INVENTORY

    override var selectedItem: InventoryItem? = null

    val craftingItems = MutableList<InventoryItem?>(9) { null }

    var craftResult: InventoryItem? = null
}