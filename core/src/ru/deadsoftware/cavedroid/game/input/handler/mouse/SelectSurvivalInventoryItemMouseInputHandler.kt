package ru.deadsoftware.cavedroid.game.input.handler.mouse

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideWindow
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.windows.inventory.SurvivalInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class SelectSurvivalInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IGameInputHandler<MouseInputAction> {

    private val survivalWindowTexture get() = requireNotNull(Assets.textureRegions["survival"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.SURVIVAL_INVENTORY &&
                isInsideWindow(action, survivalWindowTexture) &&
                (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Right || action.actionKey is MouseInputActionKey.Touch)
                && action.actionKey.touchUp
    }

    private fun onLeftCLick(items: MutableList<InventoryItem?>, window: SurvivalInventoryWindow, index: Int) {
        val selectedItem = window.selectedItem
        val clickedItem = items[index]

        if (clickedItem != null && selectedItem != null && items[index]!!.item == selectedItem.item &&
            items[index]!!.amount + selectedItem.amount <= selectedItem.item.params.maxStack) {
            items[index]!!.amount += selectedItem.amount
            window.selectedItem = null
            return
        }

        val item = items[index]
        items[index] = selectedItem ?: gameItemsHolder.fallbackItem.toInventoryItem()
        window.selectedItem = item
    }

    private fun onRightClick(items: MutableList<InventoryItem?>, window: SurvivalInventoryWindow, index: Int) {
        val clickedItem = items[index]
        val selectedItem = window.selectedItem
            ?.takeIf { clickedItem == null || clickedItem.item.isNone() || it.item == items[index]!!.item && items[index]!!.amount + 1 < it.item.params.maxStack }
            ?: return

        val newItem = selectedItem.item.toInventoryItem((clickedItem?.takeIf { !it.item.isNone() }?.amount ?: 0) + 1)
        items[index] = newItem
        selectedItem.amount --

        if (selectedItem.amount <= 0) {
            window.selectedItem = null
        }
    }

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        var itemIndex = ((xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Survival.itemsInRow))
        itemIndex += GameWindowsConfigs.Survival.hotbarCells

        if (itemIndex >= 36) {
            itemIndex -= 36
        }

        if (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Touch) {
            onLeftCLick(mobsController.player.inventory.items as MutableList<InventoryItem?>, window, itemIndex)
        } else {
            onRightClick(mobsController.player.inventory.items as MutableList<InventoryItem?>, window, itemIndex)
        }

        Gdx.app.debug(
            TAG,
            "selected item: ${window.selectedItem?.item?.params?.key ?: "null"}; index $itemIndex, grid ($xOnGrid;$yOnGrid)"
        )
    }

    private fun handleInsideCraft(action: MouseInputAction, xOnCraft: Int, yOnCraft: Int) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow
        val index = xOnCraft + yOnCraft * GameWindowsConfigs.Crafting.craftGridSize // this is crafting on purpose!!

        if (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Touch) {
            onLeftCLick(window.craftingItems, window, index)
        } else {
            onRightClick(window.craftingItems, window, index)
        }

        window.craftResult =
            gameItemsHolder.craftItem(window.craftingItems.map { it?.item ?: gameItemsHolder.fallbackItem })
    }

    override fun handle(action: MouseInputAction) {
        val survivalTexture = survivalWindowTexture
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        val xOnWindow = action.screenX - (action.cameraViewport.width / 2 - survivalTexture.regionWidth / 2)
        val yOnWindow = action.screenY - (action.cameraViewport.height / 2 - survivalTexture.regionHeight / 2)

        val xOnGrid = (xOnWindow - GameWindowsConfigs.Survival.itemsGridMarginLeft) /
                GameWindowsConfigs.Survival.itemsGridColWidth
        val yOnGrid = (yOnWindow - GameWindowsConfigs.Survival.itemsGridMarginTop) /
                GameWindowsConfigs.Survival.itemsGridRowHeight

        val xOnCraft = (xOnWindow - GameWindowsConfigs.Survival.craftOffsetX) /
                GameWindowsConfigs.Survival.itemsGridColWidth
        val yOnCraft = (yOnWindow - GameWindowsConfigs.Survival.craftOffsetY) /
                GameWindowsConfigs.Survival.itemsGridRowHeight

        val isInsideInventoryGrid = xOnGrid >= 0 && xOnGrid < GameWindowsConfigs.Survival.itemsInRow &&
                yOnGrid >= 0 && yOnGrid < GameWindowsConfigs.Survival.itemsInCol

        val isInsideCraftGrid = xOnCraft >= 0 && xOnCraft < GameWindowsConfigs.Survival.craftGridSize &&
                yOnCraft >= 0 && yOnCraft < GameWindowsConfigs.Survival.craftGridSize

        val isInsideCraftResult = xOnWindow > GameWindowsConfigs.Survival.craftResultOffsetX &&
                xOnWindow < GameWindowsConfigs.Survival.craftResultOffsetX + GameWindowsConfigs.Survival.itemsGridColWidth &&
                yOnWindow > GameWindowsConfigs.Survival.craftResultOffsetY &&
                yOnWindow < GameWindowsConfigs.Survival.craftResultOffsetY + GameWindowsConfigs.Survival.itemsGridRowHeight

        if (isInsideInventoryGrid) {
            handleInsideInventoryGrid(action, xOnGrid.toInt(), yOnGrid.toInt())
        } else if (isInsideCraftGrid) {
            handleInsideCraft(action, xOnCraft.toInt(), yOnCraft.toInt())
        } else if (isInsideCraftResult) {
            val selectedItem = window.selectedItem
            if (selectedItem == null || selectedItem.item.isNone() ||
                (selectedItem.item == window.craftResult?.item && selectedItem.amount + (window.craftResult?.amount ?: 0) <= selectedItem.item.params.maxStack)) {
                for (i in window.craftingItems.indices) {
                    if ((window.craftingItems[i]?.amount ?: 0) > 1) {
                        window.craftingItems[i]?.amount = window.craftingItems[i]?.amount!! - 1
                    } else {
                        window.craftingItems[i] = null
                    }
                }
                if (selectedItem != null && !selectedItem.item.isNone()) {
                    selectedItem.amount += (window.craftResult?.amount ?: 0)
                } else {
                    window.selectedItem = window.craftResult
                }
                window.craftResult = gameItemsHolder.craftItem(window.craftingItems
                    .map { it?.item ?: gameItemsHolder.fallbackItem })
            }
        }

    }

    companion object {
        private const val TAG = "SelectSurvivalInventoryItemMouseInputHandler"

    }
}