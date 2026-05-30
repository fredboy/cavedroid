package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import co.touchlab.kermit.Logger
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.api.InventoryHintEvents
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

abstract class AbstractInventoryItemsMouseInputHandler(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val itemsRepository: ItemsRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val windowType: GameWindowType,
    private val inventoryHintEvents: InventoryHintEvents,
    private val statsRepository: StatsRepository,
    private val playerAdapter: PlayerAdapter,
    private val dropQueue: DropQueue,
) : IMouseInputHandler {

    private var selectableCellHoldTask: Timer.Task? = null
    private var holdingPointer: Int = -1
    private var onCancelHold: (() -> Unit)? = null

    protected abstract val windowTexture: TextureRegion

    override fun reset() {
        cancelHold()
    }

    protected abstract fun getWindowRect(viewport: Rectangle): Rectangle

    private fun cancelHold() {
        selectableCellHoldTask?.cancel()
        selectableCellHoldTask = null
        holdingPointer = -1
        onCancelHold?.invoke()
        onCancelHold = null
    }

    private fun handleHold(
        items: MutableList<InventoryItem>,
        window: AbstractInventoryWindow,
        index: Int,
        pointer: Int,
    ) {
        onCancelHold = null
        cancelHold()

        if (!window.selectedItem.isNoneOrNull()) {
            return
        }

        window.onRightClick(items, itemsRepository, index, pointer)

        inventoryHintEvents.notifyItemHeld()
    }

    private fun handleDown(
        items: MutableList<InventoryItem>,
        window: AbstractInventoryWindow,
        index: Int,
        pointer: Int,
    ) {
        cancelHold()
        onCancelHold = {
            window.onLeftCLick(items, itemsRepository, index, pointer)
            if (window is AbstractInventoryWindowWithCraftGrid) {
                updateCraftResult(window)
            }
        }
        holdingPointer = pointer
        selectableCellHoldTask = object : Timer.Task() {
            override fun run() {
                handleHold(items, window, index, pointer)
            }
        }
        Timer.schedule(selectableCellHoldTask, TOUCH_HOLD_TIME_SEC)
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.currentWindowType == windowType &&
            getWindowRect(gameContextRepository.getCameraContext().viewport).contains(
                action.screenX,
                action.screenY,
            ) &&
            (
                action.actionKey is MouseInputActionKey.Left ||
                    action.actionKey is MouseInputActionKey.Right ||
                    action.actionKey is MouseInputActionKey.Screen ||
                    action.actionKey is MouseInputActionKey.Dragged &&
                    applicationContextRepository.isTouch()
                ) &&
            (
                action.actionKey is MouseInputActionKey.Dragged ||
                    action.actionKey.touchUp ||
                    action.actionKey is MouseInputActionKey.Screen
                )
    }

    protected fun updateCraftResult(window: AbstractInventoryWindowWithCraftGrid) {
        window.craftResult = itemsRepository.getCraftingResult(window.craftingItems)
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
        if (action.actionKey is MouseInputActionKey.Screen && action.actionKey.touchUp) {
            cancelHold()
        }

        if (action.actionKey is MouseInputActionKey.Dragged) {
            if (action.actionKey.pointer == holdingPointer) {
                cancelHold()
            }
        } else if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp && window.selectedItem.isNoneOrNull()) {
                handleDown(items, window, index, action.actionKey.pointer)
            } else if (!action.actionKey.touchUp) {
                window.onLeftCLick(items, itemsRepository, index, action.actionKey.pointer)
            } else if (!window.selectedItem.isNoneOrNull()) {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(items, itemsRepository, index, action.actionKey.pointer)
                } else {
                    window.onRightClick(items, itemsRepository, index, action.actionKey.pointer)
                    inventoryHintEvents.notifyItemPlacedByOne()
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left) {
            window.onLeftCLick(items, itemsRepository, index)
        } else {
            window.onRightClick(items, itemsRepository, index)
            inventoryHintEvents.notifyItemPlacedByOne()
        }
        inventoryHintEvents.notifyItemMoved()
    }

    protected fun handleInsideCraftResultCell(
        action: MouseInputAction,
        items: MutableList<InventoryItem>,
        window: AbstractInventoryWindow,
        index: Int,
    ) {
        if (action.actionKey is MouseInputActionKey.Dragged) {
            return
        }

        val selectedItem = window.selectedItem

        if (items[index].isNoneOrNull() ||
            !selectedItem.isNoneOrNull() &&
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

        val consumed = items[index].isNoneOrNull()

        if (window is AbstractInventoryWindowWithCraftGrid) {
            reduceCraftItems(window)
            if (consumed) {
                statsRepository.recordItemCrafted()
            }
        }
    }

    protected fun handleOutsideAnyCell(action: MouseInputAction, window: AbstractInventoryWindow) {
        if (action.actionKey !is MouseInputActionKey.Screen || !action.actionKey.touchUp) {
            return
        }

        val selectedItem = window.selectedItem?.takeIf { !it.isNoneOrNull() } ?: return

        dropQueue.offerItem(playerAdapter.x, playerAdapter.y, selectedItem)
        window.selectedItem = null
    }

    companion object {
        private val logger = Logger.withTag("AbstractInventoryItemsMouseInputHandler")
        private const val TOUCH_HOLD_TIME_SEC = 0.5f
    }
}
