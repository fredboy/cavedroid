package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.SurvivalInventoryWindow
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class SelectSurvivalInventoryItemMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    itemsRepository: ItemsRepository,
) : AbstractInventoryItemsMouseInputHandler(
    gameContextRepository = gameContextRepository,
    itemsRepository = itemsRepository,
    gameWindowsManager = gameWindowsManager,
    windowType = GameWindowType.SURVIVAL_INVENTORY,
) {

    override val windowTexture get() = requireNotNull(textureRegions["survival"])

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        var itemIndex = xOnGrid + yOnGrid * GameWindowsConfigs.Survival.itemsInRow
        itemIndex += GameWindowsConfigs.Survival.hotbarCells

        if (itemIndex >= mobController.player.inventory.size) {
            itemIndex -= mobController.player.inventory.size
        }

        handleInsidePlaceableCell(action, mobController.player.inventory.items, window, itemIndex)
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
        val xOnWindow =
            action.screenX - (gameContextRepository.getCameraContext().viewport.width / 2 - windowTexture.regionWidth / 2)
        val yOnWindow =
            action.screenY - (gameContextRepository.getCameraContext().viewport.height / 2 - windowTexture.regionHeight / 2)

        val xOnGrid = (xOnWindow - GameWindowsConfigs.Survival.itemsGridMarginLeft) /
            GameWindowsConfigs.Survival.itemsGridColWidth
        val yOnGrid = (yOnWindow - GameWindowsConfigs.Survival.itemsGridMarginTop) /
            GameWindowsConfigs.Survival.itemsGridRowHeight

        val xOnCraft = (xOnWindow - GameWindowsConfigs.Survival.craftOffsetX) /
            GameWindowsConfigs.Survival.itemsGridColWidth
        val yOnCraft = (yOnWindow - GameWindowsConfigs.Survival.craftOffsetY) /
            GameWindowsConfigs.Survival.itemsGridRowHeight

        val isInsideInventoryGrid = xOnGrid >= 0 &&
            xOnGrid < GameWindowsConfigs.Survival.itemsInRow &&
            yOnGrid >= 0 &&
            yOnGrid < GameWindowsConfigs.Survival.itemsInCol

        val isInsideCraftGrid = xOnCraft >= 0 &&
            xOnCraft < GameWindowsConfigs.Survival.craftGridSize &&
            yOnCraft >= 0 &&
            yOnCraft < GameWindowsConfigs.Survival.craftGridSize

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
