package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.isInsideWindow

abstract class AbstractInventoryItemsMouseInputHandler(
    private val gameContextRepository: GameContextRepository,
    private val itemsRepository: ItemsRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val windowType: GameWindowType,
) : IMouseInputHandler {

    protected abstract val windowTexture: TextureRegion

    override fun checkConditions(action: MouseInputAction): Boolean = gameWindowsManager.currentWindowType == windowType &&
        isInsideWindow(gameContextRepository, action, windowTexture) &&
        (
            action.actionKey is MouseInputActionKey.Left ||
                action.actionKey is MouseInputActionKey.Right ||
                action.actionKey is MouseInputActionKey.Screen
            ) &&
        (action.actionKey.touchUp || action.actionKey is MouseInputActionKey.Screen)

    protected fun updateCraftResult(window: AbstractInventoryWindowWithCraftGrid) {
        window.craftResult = itemsRepository.getCraftingResult(window.craftingItems.map(InventoryItem::item))
    }

    private fun reduceCraftItems(window: AbstractInventoryWindowWithCraftGrid) {
        for (i in window.craftingItems.indices) {
            if (window.craftingItems[i].amount > 1) {
                window.craftingItems[i].amount--
            } else {
                window.craftingItems[i] = itemsRepository.fallbackItem.toInventoryItem()
            }
        }
    }

    protected fun handleInsidePlaceableCell(
        action: MouseInputAction,
        items: MutableList<InventoryItem>,
        window: AbstractInventoryWindow,
        index: Int,
    ) {
        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                window.onLeftCLick(items, itemsRepository, index, action.actionKey.pointer)
            } else {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(items, itemsRepository, index, action.actionKey.pointer)
                } else {
                    window.onRightClick(items, itemsRepository, index)
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left) {
            window.onLeftCLick(items, itemsRepository, index)
        } else {
            window.onRightClick(items, itemsRepository, index)
        }
    }

    protected fun handleInsideCraftResultCell(
        action: MouseInputAction,
        items: MutableList<InventoryItem>,
        window: AbstractInventoryWindow,
        index: Int,
    ) {
        val selectedItem = window.selectedItem

        if (!selectedItem.isNoneOrNull() &&
            (
                selectedItem.item != items[index].item ||
                    !selectedItem.canBeAdded(items[index].amount)
                )
        ) {
            return
        }

        if (!selectedItem.isNoneOrNull()) {
            selectedItem.amount += items[index].amount
            items[index] = itemsRepository.fallbackItem.toInventoryItem()
        } else {
            if (action.actionKey is MouseInputActionKey.Screen) {
                if (!action.actionKey.touchUp) {
                    window.onLeftCLick(items, itemsRepository, index, action.actionKey.pointer)
                }
            } else if (action.actionKey is MouseInputActionKey.Left) {
                window.onLeftCLick(items, itemsRepository, index)
            }
        }

        if (window is AbstractInventoryWindowWithCraftGrid) {
            reduceCraftItems(window)
        }
    }
}
