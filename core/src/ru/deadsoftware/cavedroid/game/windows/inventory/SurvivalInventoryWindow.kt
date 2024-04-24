package ru.deadsoftware.cavedroid.game.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

class SurvivalInventoryWindow(
    override val type: GameUiWindow,
) : AbstractInventoryWindow() {

    override var selectedItem: InventoryItem? = null

    val craftingItems = MutableList<InventoryItem?>(9) { null }

    var craftResult: InventoryItem? = null
}