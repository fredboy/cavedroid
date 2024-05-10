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
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.CraftingInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class SelectCraftingInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
    private val dropController: DropController,
) : IGameInputHandler<MouseInputAction> {

    private val survivalWindowTexture get() = requireNotNull(Assets.textureRegions["survival"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.CRAFTING_TABLE &&
                isInsideWindow(action, survivalWindowTexture) &&
                (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Right || action.actionKey is MouseInputActionKey.Screen)
                && (action.actionKey.touchUp || action.actionKey is MouseInputActionKey.Screen)
    }

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow

        var itemIndex = ((xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Crafting.itemsInRow))
        itemIndex += GameWindowsConfigs.Crafting.hotbarCells

        if (itemIndex >= mobsController.player.inventory.size) {
            itemIndex -= mobsController.player.inventory.size
        }

        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                window.onLeftCLick(mobsController.player.inventory.items as MutableList<InventoryItem?>, gameItemsHolder, itemIndex, action.actionKey.pointer)
            } else {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(mobsController.player.inventory.items as MutableList<InventoryItem?>, gameItemsHolder, itemIndex, action.actionKey.pointer)
                } else {
                    window.onRightClick(mobsController.player.inventory.items as MutableList<InventoryItem?>, itemIndex)
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left) {
            window.onLeftCLick(mobsController.player.inventory.items as MutableList<InventoryItem?>, gameItemsHolder, itemIndex)
        } else {
            window.onRightClick(mobsController.player.inventory.items as MutableList<InventoryItem?>, itemIndex)
        }

        Gdx.app.debug(
            TAG,
            "selected item: ${window.selectedItem?.item?.params?.key ?: "null"}; index $itemIndex, grid ($xOnGrid;$yOnGrid)"
        )
    }

    private fun handleInsideCraft(action: MouseInputAction, xOnCraft: Int, yOnCraft: Int) {
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow
        val index = xOnCraft + yOnCraft * GameWindowsConfigs.Crafting.craftGridSize

        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                window.onLeftCLick(window.craftingItems, gameItemsHolder, index, action.actionKey.pointer)
            } else {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(window.craftingItems, gameItemsHolder, index, action.actionKey.pointer)
                } else {
                    window.onRightClick(window.craftingItems, index)
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) {
            window.onLeftCLick(window.craftingItems, gameItemsHolder, index)
        } else {
            window.onRightClick(window.craftingItems, index)
        }

        window.craftResult =
            gameItemsHolder.craftItem(window.craftingItems.map { it?.item ?: gameItemsHolder.fallbackItem })
    }

    override fun handle(action: MouseInputAction) {
        val survivalTexture = survivalWindowTexture
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow

        val xOnWindow = action.screenX - (action.cameraViewport.width / 2 - survivalTexture.regionWidth / 2)
        val yOnWindow = action.screenY - (action.cameraViewport.height / 2 - survivalTexture.regionHeight / 2)

        val xOnGrid = (xOnWindow - GameWindowsConfigs.Crafting.itemsGridMarginLeft) /
                GameWindowsConfigs.Crafting.itemsGridColWidth
        val yOnGrid = (yOnWindow - GameWindowsConfigs.Crafting.itemsGridMarginTop) /
                GameWindowsConfigs.Crafting.itemsGridRowHeight

        val xOnCraft = (xOnWindow - GameWindowsConfigs.Crafting.craftOffsetX) /
                GameWindowsConfigs.Crafting.itemsGridColWidth
        val yOnCraft = (yOnWindow - GameWindowsConfigs.Crafting.craftOffsetY) /
                GameWindowsConfigs.Crafting.itemsGridRowHeight

        val isInsideInventoryGrid = xOnGrid >= 0 && xOnGrid < GameWindowsConfigs.Crafting.itemsInRow &&
                yOnGrid >= 0 && yOnGrid < GameWindowsConfigs.Crafting.itemsInCol

        val isInsideCraftGrid = xOnCraft >= 0 && xOnCraft < GameWindowsConfigs.Crafting.craftGridSize &&
                yOnCraft >= 0 && yOnCraft < GameWindowsConfigs.Crafting.craftGridSize

        val isInsideCraftResult = xOnWindow > GameWindowsConfigs.Crafting.craftResultOffsetX &&
                xOnWindow < GameWindowsConfigs.Crafting.craftResultOffsetX + GameWindowsConfigs.Crafting.itemsGridColWidth &&
                yOnWindow > GameWindowsConfigs.Crafting.craftResultOffsetY &&
                yOnWindow < GameWindowsConfigs.Crafting.craftResultOffsetY + GameWindowsConfigs.Crafting.itemsGridRowHeight

        if (isInsideInventoryGrid) {
            handleInsideInventoryGrid(action, xOnGrid.toInt(), yOnGrid.toInt())
        } else if (isInsideCraftGrid) {
            handleInsideCraft(action, xOnCraft.toInt(), yOnCraft.toInt())
        } else if (isInsideCraftResult && action.actionKey.touchUp) {
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
        private const val TAG = "SelectCraftingInventoryItemMouseInputHandler"

    }
}