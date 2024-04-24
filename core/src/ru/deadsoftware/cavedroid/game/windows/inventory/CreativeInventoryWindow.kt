package ru.deadsoftware.cavedroid.game.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

class CreativeInventoryWindow(
    override val type: GameUiWindow,
) : AbstractInventoryWindow() {
    override var selectedItem: InventoryItem? = null
}