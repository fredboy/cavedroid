package ru.fredboy.cavedroid.ux.controls.input.handler.mouse

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.CraftingInventoryWindow
import ru.fredboy.cavedroid.ux.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class SelectCraftingInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    itemsRepository: ItemsRepository,
) : AbstractInventoryItemsMouseInputHandler(itemsRepository, gameWindowsManager, GameWindowType.CRAFTING_TABLE) {

    override val windowTexture get() = requireNotNull(textureRegions["crafting_table"])

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow

        var itemIndex = xOnGrid + yOnGrid * GameWindowsConfigs.Crafting.itemsInRow
        itemIndex += GameWindowsConfigs.Crafting.hotbarCells

        if (itemIndex >= mobController.player.inventory.size) {
            itemIndex -= mobController.player.inventory.size
        }

        handleInsidePlaceableCell(action, mobController.player.inventory.items, window, itemIndex)
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

        val isInsideInventoryGrid = xOnGrid >= 0 &&
            xOnGrid < GameWindowsConfigs.Crafting.itemsInRow &&
            yOnGrid >= 0 &&
            yOnGrid < GameWindowsConfigs.Crafting.itemsInCol

        val isInsideCraftGrid = xOnCraft >= 0 &&
            xOnCraft < GameWindowsConfigs.Crafting.craftGridSize &&
            yOnCraft >= 0 &&
            yOnCraft < GameWindowsConfigs.Crafting.craftGridSize

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
