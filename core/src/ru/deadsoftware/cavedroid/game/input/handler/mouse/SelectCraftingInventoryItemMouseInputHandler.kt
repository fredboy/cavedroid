package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.mobs.MobsController
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
) : AbstractInventoryItemsMouseInputHandler(gameItemsHolder, gameWindowsManager, GameUiWindow.CRAFTING_TABLE) {

    override val windowTexture get() = requireNotNull(Assets.textureRegions["crafting_table"])

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow

        var itemIndex = xOnGrid + yOnGrid * GameWindowsConfigs.Crafting.itemsInRow
        itemIndex += GameWindowsConfigs.Crafting.hotbarCells

        if (itemIndex >= mobsController.player.inventory.size) {
            itemIndex -= mobsController.player.inventory.size
        }

        handleInsidePlaceableCell(action, mobsController.player.inventory.items, window, itemIndex)
    }

    private fun handleInsideCraft(action: MouseInputAction, xOnCraft: Int, yOnCraft: Int) {
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow
        val index = xOnCraft + yOnCraft * GameWindowsConfigs.Crafting.craftGridSize

        handleInsidePlaceableCell(action, window.craftingItems, window, index)

        updateCraftResult(window)
    }

    private fun handleInsideCraftResult(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow

        handleInsideCraftResultCell(action, window.craftResultList, window, 0)

        updateCraftResult(window)
    }

    override fun handle(action: MouseInputAction) {
        val texture = windowTexture

        val xOnWindow = action.screenX - (action.cameraViewport.width / 2 - texture.regionWidth / 2)
        val yOnWindow = action.screenY - (action.cameraViewport.height / 2 - texture.regionHeight / 2)

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
        } else if (isInsideCraftResult) {
            handleInsideCraftResult(action)
        }

    }
}