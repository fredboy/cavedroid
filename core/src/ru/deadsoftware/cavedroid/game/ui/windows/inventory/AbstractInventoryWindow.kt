package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

abstract class AbstractInventoryWindow {

    abstract val type: GameUiWindow

    abstract var selectedItem: InventoryItem?

}