package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

class CreativeInventoryWindow() : AbstractInventoryWindow() {

    override val type = GameUiWindow.CREATIVE_INVENTORY

    override var selectedItem: InventoryItem? = null
}