package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import com.badlogic.gdx.math.MathUtils
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem.Companion.isNoneOrNull

abstract class AbstractInventoryWindow {

    abstract val type: GameUiWindow

    abstract var selectedItem: InventoryItem?

    var selectItemPointer: Int = -1

    fun onLeftCLick(
        items: MutableList<InventoryItem>,
        gameItemsHolder: GameItemsHolder,
        index: Int,
        pointer: Int = -1
    ) {
        if (selectedItem != null &&
            selectedItem?.item?.isNone() != true &&
            pointer >= 0 && selectItemPointer >= 0 &&
            pointer != selectItemPointer
        ) {
            return
        }

        val clickedItem = items[index]

        selectedItem?.let { selectedItem ->
            if (!clickedItem.isNoneOrNull() && items[index].item == selectedItem.item &&
                items[index].amount + selectedItem.amount <= selectedItem.item.params.maxStack
            ) {
                items[index].amount += selectedItem.amount
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

    fun onRightClick(items: MutableList<InventoryItem>, gameItemsHolder: GameItemsHolder, index: Int) {
        val clickedItem = items[index]
        val selectedItem = selectedItem

        if (selectedItem.isNoneOrNull() && !clickedItem.isNoneOrNull()) {
            val half = InventoryItem(clickedItem.item, MathUtils.ceil(clickedItem.amount.toFloat() / 2f))
            this.selectedItem = half
            clickedItem.subtract(half.amount)
            if (clickedItem.amount == 0) {
                items[index] = gameItemsHolder.fallbackItem.toInventoryItem()
            }
            return
        }

        if (selectedItem == null ||
            (!clickedItem.isNoneOrNull() && selectedItem.item != clickedItem.item) ||
            !clickedItem.canBeAdded()) {
            return
        }

        val newItem = selectedItem.item.toInventoryItem(
            (clickedItem.takeIf { !it.item.isNone() }?.amount ?: 0) + 1
        )
        items[index] = newItem
        selectedItem.amount--

        if (selectedItem.amount <= 0) {
            this@AbstractInventoryWindow.selectedItem = null
        }
    }

}