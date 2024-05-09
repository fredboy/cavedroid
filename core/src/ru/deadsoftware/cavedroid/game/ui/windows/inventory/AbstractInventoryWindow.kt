package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem

abstract class AbstractInventoryWindow {

    abstract val type: GameUiWindow

    abstract var selectedItem: InventoryItem?

    var selectItemPointer: Int = -1

    fun onLeftCLick(items: MutableList<InventoryItem?>, gameItemsHolder: GameItemsHolder, index: Int, pointer: Int = -1) {
        if (selectedItem != null && selectedItem?.item?.isNone() != true && pointer >= 0 && selectItemPointer >= 0 && pointer != selectItemPointer) {
            return
        }

        val clickedItem = items[index]

        selectedItem?.let { selectedItem ->
            if (clickedItem != null && items[index]!!.item == selectedItem.item &&
                items[index]!!.amount + selectedItem.amount <= selectedItem.item.params.maxStack) {
                items[index]!!.amount += selectedItem.amount
                this@AbstractInventoryWindow.selectedItem = null
                selectItemPointer = -1
                return
            }
        }

        val item = items[index]
        items[index] = selectedItem ?: gameItemsHolder.fallbackItem.toInventoryItem()
        selectedItem = item
        selectItemPointer = pointer
    }

    fun onRightClick(items: MutableList<InventoryItem?>, index: Int) {
        val clickedItem = items[index]
        val selectedItem = selectedItem
            ?.takeIf { clickedItem == null || clickedItem.item.isNone() || it.item == items[index]!!.item && items[index]!!.amount + 1 < it.item.params.maxStack }
            ?: return

        val newItem = selectedItem.item.toInventoryItem((clickedItem?.takeIf { !it.item.isNone() }?.amount ?: 0) + 1)
        items[index] = newItem
        selectedItem.amount --

        if (selectedItem.amount <= 0) {
            this@AbstractInventoryWindow.selectedItem = null
        }
    }

}