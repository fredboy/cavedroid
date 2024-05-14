package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.MouseInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.SurvivalInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
@MouseInputHandler
class SelectSurvivalInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : AbstractInventoryItemsMouseInputHandler(gameItemsHolder, gameWindowsManager, GameUiWindow.SURVIVAL_INVENTORY) {

    override val windowTexture get() = requireNotNull(Assets.textureRegions["survival"])

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        var itemIndex = xOnGrid + yOnGrid * GameWindowsConfigs.Survival.itemsInRow
        itemIndex += GameWindowsConfigs.Survival.hotbarCells

        if (itemIndex >= mobsController.player.inventory.size) {
            itemIndex -= mobsController.player.inventory.size
        }

        handleInsidePlaceableCell(action, mobsController.player.inventory.items, window, itemIndex)
    }

    private fun handleInsideCraft(action: MouseInputAction, xOnCraft: Int, yOnCraft: Int) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow
        val index = xOnCraft + yOnCraft * GameWindowsConfigs.Crafting.craftGridSize // this is crafting on purpose!!

        handleInsidePlaceableCell(action, window.craftingItems, window, index)

        updateCraftResult(window)
    }

    private fun handleInsideCraftResult(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        handleInsideCraftResultCell(action, window.craftResultList, window, 0)

        updateCraftResult(window)
    }

    override fun handle(action: MouseInputAction) {
        val xOnWindow = action.screenX - (action.cameraViewport.width / 2 - windowTexture.regionWidth / 2)
        val yOnWindow = action.screenY - (action.cameraViewport.height / 2 - windowTexture.regionHeight / 2)

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
            handleInsideCraftResult(action)
        }

    }
}