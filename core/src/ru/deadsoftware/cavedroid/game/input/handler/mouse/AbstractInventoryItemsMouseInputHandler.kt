package ru.deadsoftware.cavedroid.game.input.handler.mouse

import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideWindow
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem.Companion.isNoneOrNull
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.AbstractInventoryWindow
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.AbstractInventoryWindowWithCraftGrid

abstract class AbstractInventoryItemsMouseInputHandler(
    private val gameItemsHolder: GameItemsHolder,
    private val gameWindowsManager: GameWindowsManager,
    private val windowType: GameUiWindow,
) : IGameInputHandler<MouseInputAction> {

    protected abstract val windowTexture: TextureRegion

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == windowType &&
                isInsideWindow(action, windowTexture) &&
                (action.actionKey is MouseInputActionKey.Left ||
                        action.actionKey is MouseInputActionKey.Right ||
                        action.actionKey is MouseInputActionKey.Screen)
                && (action.actionKey.touchUp || action.actionKey is MouseInputActionKey.Screen)
    }

    protected fun updateCraftResult(window: AbstractInventoryWindowWithCraftGrid) {
        window.craftResult = gameItemsHolder.craftItem(window.craftingItems.map(InventoryItem::item))
            ?: gameItemsHolder.fallbackItem.toInventoryItem()
    }

    private fun reduceCraftItems(window: AbstractInventoryWindowWithCraftGrid) {
        for (i in window.craftingItems.indices) {
            if (window.craftingItems[i].amount > 1) {
                window.craftingItems[i].amount--
            } else {
                window.craftingItems[i] = gameItemsHolder.fallbackItem.toInventoryItem()
            }
        }
    }

    protected fun handleInsidePlaceableCell(
        action: MouseInputAction,
        items: MutableList<InventoryItem>,
        window: AbstractInventoryWindow,
        index: Int
    ) {
        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                window.onLeftCLick(items, gameItemsHolder, index, action.actionKey.pointer)
            } else {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(items, gameItemsHolder, index, action.actionKey.pointer)
                } else {
                    window.onRightClick(items, gameItemsHolder, index)
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left) {
            window.onLeftCLick(items, gameItemsHolder, index)
        } else {
            window.onRightClick(items, gameItemsHolder, index)
        }
    }

    protected fun handleInsideCraftResultCell(
        action: MouseInputAction,
        items: MutableList<InventoryItem>,
        window: AbstractInventoryWindow,
        index: Int
    ) {
        val selectedItem = window.selectedItem

        if (!selectedItem.isNoneOrNull() && (selectedItem.item != items[index].item ||
                    !selectedItem.canBeAdded(items[index].amount))) {
            return
        }

        if (!selectedItem.isNoneOrNull()) {
            selectedItem.amount += items[index].amount
            items[index] = gameItemsHolder.fallbackItem.toInventoryItem()
        } else {
            if (action.actionKey is MouseInputActionKey.Screen) {
                if (!action.actionKey.touchUp) {
                    window.onLeftCLick(items, gameItemsHolder, index, action.actionKey.pointer)
                }
            } else if (action.actionKey is MouseInputActionKey.Left) {
                window.onLeftCLick(items, gameItemsHolder, index)
            }
        }

        if (window is AbstractInventoryWindowWithCraftGrid) {
            reduceCraftItems(window)
        }

    }

}