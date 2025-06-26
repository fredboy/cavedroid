package ru.fredboy.cavedroid.ux.controls.input.handler.mouse

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.FurnaceInventoryWindow
import ru.fredboy.cavedroid.ux.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class SelectFurnaceInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    itemsRepository: ItemsRepository
) : AbstractInventoryItemsMouseInputHandler(itemsRepository, gameWindowsManager, GameWindowType.FURNACE) {

    override val windowTexture get() = requireNotNull(textureRegions["furnace"])

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        var itemIndex = xOnGrid + yOnGrid * GameWindowsConfigs.Furnace.itemsInRow
        itemIndex += GameWindowsConfigs.Furnace.hotbarCells

        if (itemIndex >= mobController.player.inventory.size) {
            itemIndex -= mobController.player.inventory.size
        }

        handleInsidePlaceableCell(action, mobController.player.inventory.items, window, itemIndex)
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