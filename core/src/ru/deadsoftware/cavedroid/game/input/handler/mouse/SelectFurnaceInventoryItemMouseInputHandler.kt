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
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem.Companion.isNoneOrNull
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.game.objects.container.Furnace
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.FurnaceInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class SelectFurnaceInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
    private val dropController: DropController,
) : IGameInputHandler<MouseInputAction> {

    private val survivalWindowTexture get() = requireNotNull(Assets.textureRegions["survival"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.FURNACE &&
                isInsideWindow(action, survivalWindowTexture) &&
                (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Right || action.actionKey is MouseInputActionKey.Screen)
                && (action.actionKey.touchUp || action.actionKey is MouseInputActionKey.Screen)
    }

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        var itemIndex = ((xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Furnace.itemsInRow))
        itemIndex += GameWindowsConfigs.Furnace.hotbarCells

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

    private fun handleInsideFuel(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        if (!window.selectedItem.isNoneOrNull() && window.selectedItem?.item?.params?.burningTimeMs == null) {
            return
        }

        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                window.onLeftCLick(window.furnace.items as MutableList<InventoryItem?>, gameItemsHolder, Furnace.FUEL_INDEX, action.actionKey.pointer)
            } else {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(window.furnace.items as MutableList<InventoryItem?>, gameItemsHolder, Furnace.FUEL_INDEX, action.actionKey.pointer)
                } else {
                    window.onRightClick(window.furnace.items as MutableList<InventoryItem?>, Furnace.FUEL_INDEX)
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) {
            window.onLeftCLick(window.furnace.items as MutableList<InventoryItem?>, gameItemsHolder, Furnace.FUEL_INDEX)
        } else {
            window.onRightClick(window.furnace.items as MutableList<InventoryItem?>, Furnace.FUEL_INDEX)
        }
    }

    private fun handleInsideInput(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                window.onLeftCLick(window.furnace.items as MutableList<InventoryItem?>, gameItemsHolder, Furnace.INPUT_INDEX, action.actionKey.pointer)
            } else {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(window.furnace.items as MutableList<InventoryItem?>, gameItemsHolder, Furnace.INPUT_INDEX, action.actionKey.pointer)
                } else {
                    window.onRightClick(window.furnace.items as MutableList<InventoryItem?>, Furnace.INPUT_INDEX)
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) {
            window.onLeftCLick(window.furnace.items as MutableList<InventoryItem?>, gameItemsHolder, Furnace.INPUT_INDEX)
        } else {
            window.onRightClick(window.furnace.items as MutableList<InventoryItem?>, Furnace.INPUT_INDEX)
        }
    }

    override fun handle(action: MouseInputAction) {
        val survivalTexture = survivalWindowTexture
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        val xOnWindow = action.screenX - (action.cameraViewport.width / 2 - survivalTexture.regionWidth / 2)
        val yOnWindow = action.screenY - (action.cameraViewport.height / 2 - survivalTexture.regionHeight / 2)

        val xOnGrid = (xOnWindow - GameWindowsConfigs.Furnace.itemsGridMarginLeft) /
                GameWindowsConfigs.Furnace.itemsGridColWidth
        val yOnGrid = (yOnWindow - GameWindowsConfigs.Furnace.itemsGridMarginTop) /
                GameWindowsConfigs.Furnace.itemsGridRowHeight

        val isInsideInput = xOnWindow > GameWindowsConfigs.Furnace.smeltInputMarginLeft &&
                xOnWindow < GameWindowsConfigs.Furnace.smeltInputMarginLeft + GameWindowsConfigs.Furnace.itemsGridColWidth &&
                yOnWindow > GameWindowsConfigs.Furnace.smeltInputMarginTop &&
                yOnWindow < GameWindowsConfigs.Furnace.smeltInputMarginTop + GameWindowsConfigs.Furnace.itemsGridRowHeight

        val isInsideFuel = xOnWindow > GameWindowsConfigs.Furnace.smeltFuelMarginLeft &&
                xOnWindow < GameWindowsConfigs.Furnace.smeltFuelMarginLeft + GameWindowsConfigs.Furnace.itemsGridColWidth &&
                yOnWindow > GameWindowsConfigs.Furnace.smeltFuelMarginTop &&
                yOnWindow < GameWindowsConfigs.Furnace.smeltFuelMarginTop + GameWindowsConfigs.Furnace.itemsGridRowHeight

        val isInsideResult = xOnWindow > GameWindowsConfigs.Furnace.smeltResultOffsetX &&
                xOnWindow < GameWindowsConfigs.Furnace.smeltResultOffsetX + GameWindowsConfigs.Furnace.itemsGridColWidth &&
                yOnWindow > GameWindowsConfigs.Furnace.smeltResultOffsetY &&
                yOnWindow < GameWindowsConfigs.Furnace.smeltResultOffsetY + GameWindowsConfigs.Furnace.itemsGridRowHeight

        val isInsideInventoryGrid = xOnGrid >= 0 && xOnGrid < GameWindowsConfigs.Furnace.itemsInRow &&
                yOnGrid >= 0 && yOnGrid < GameWindowsConfigs.Furnace.itemsInCol

        if (isInsideInventoryGrid) {
            handleInsideInventoryGrid(action, xOnGrid.toInt(), yOnGrid.toInt())
        } else if (isInsideFuel) {
            handleInsideFuel(action)
        } else if (isInsideInput) {
            handleInsideInput(action)
        } else if (isInsideResult && action.actionKey.touchUp) {
            val selectedItem = window.selectedItem
            if (selectedItem == null || selectedItem.item.isNone() ||
                (selectedItem.item == window.furnace.result?.item && selectedItem.amount + (window.furnace.result?.amount ?: 0) <= selectedItem.item.params.maxStack)) {

                if (selectedItem != null && !selectedItem.item.isNone()) {
                    selectedItem.amount += (window.furnace.result?.amount ?: 0)
                } else {
                    window.selectedItem = window.furnace.result
                }
                window.furnace.items[Furnace.RESULT_INDEX] = gameItemsHolder.fallbackItem.toInventoryItem()
            }
        }

    }

    companion object {
        private const val TAG = "SelectFurnaceInventoryItemMouseInputHandler"

    }
}