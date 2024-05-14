package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindMouseInputHandler
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem.Companion.isNoneOrNull
import ru.deadsoftware.cavedroid.game.objects.container.Furnace
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.FurnaceInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class SelectFurnaceInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : AbstractInventoryItemsMouseInputHandler(gameItemsHolder, gameWindowsManager, GameUiWindow.FURNACE) {

    override val windowTexture get() = requireNotNull(Assets.textureRegions["furnace"])

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        var itemIndex = xOnGrid + yOnGrid * GameWindowsConfigs.Furnace.itemsInRow
        itemIndex += GameWindowsConfigs.Furnace.hotbarCells

        if (itemIndex >= mobsController.player.inventory.size) {
            itemIndex -= mobsController.player.inventory.size
        }

        handleInsidePlaceableCell(action, mobsController.player.inventory.items, window, itemIndex)
    }

    private fun handleInsideFuel(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        if (!window.selectedItem.isNoneOrNull() && window.selectedItem?.item?.params?.burningTimeMs == null) {
            return
        }

        handleInsidePlaceableCell(action, window.furnace.items, window, Furnace.FUEL_INDEX)
    }

    private fun handleInsideInput(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        handleInsidePlaceableCell(action, window.furnace.items, window, Furnace.INPUT_INDEX)
    }

    private fun handleInsideResult(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        handleInsideCraftResultCell(action, window.furnace.items, window, Furnace.RESULT_INDEX)
    }

    override fun handle(action: MouseInputAction) {
        val texture = windowTexture

        val xOnWindow = action.screenX - (action.cameraViewport.width / 2 - texture.regionWidth / 2)
        val yOnWindow = action.screenY - (action.cameraViewport.height / 2 - texture.regionHeight / 2)

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
        } else if (isInsideResult) {
            handleInsideResult(action)
        }

    }
}