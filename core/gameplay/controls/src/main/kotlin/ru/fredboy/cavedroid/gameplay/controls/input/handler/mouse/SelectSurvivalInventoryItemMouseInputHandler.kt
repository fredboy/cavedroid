package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.game.window.inventory.SurvivalInventoryWindow
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject
import kotlin.math.max

@GameScope
@BindMouseInputHandler
class
SelectSurvivalInventoryItemMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val itemsRepository: ItemsRepository,
    private val playerAdapter: PlayerAdapter,
    private val dropQueue: DropQueue,
) : AbstractInventoryItemsMouseInputHandler(
    gameContextRepository = gameContextRepository,
    itemsRepository = itemsRepository,
    gameWindowsManager = gameWindowsManager,
    windowType = GameWindowType.SURVIVAL_INVENTORY,
) {

    override val windowTexture get() = requireNotNull(textureRegions["survival"])

    override fun getWindowRect(viewport: Rectangle): Rectangle {
        val window = gameWindowsManager.currentWindow as? AbstractInventoryWindowWithCraftGrid
        val (recipeBookWidth, recipeBookHeight) = textureRegions["recipe_book"]
            ?.takeIf { window?.recipeBookActive == true }
            ?.run { regionWidth.toFloat() to regionHeight.toFloat() }
            ?: (0f to 0f)

        return Rectangle(
            0f,
            0f,
            recipeBookWidth + windowTexture.regionWidth.toFloat(),
            max(windowTexture.regionHeight.toFloat(), recipeBookHeight),
        ).apply {
            if (window?.recipeBookActive != true) {
                setCenter(viewport.getCenter(Vector2()))
            } else {
                x = viewport.width / 2f - recipeBookWidth
                y = viewport.height / 2f - height / 2f
            }
        }
    }

    private fun getRecipeBookRect(): Rectangle {
        return getWindowRect(gameContextRepository.getCameraContext().viewport)
            .apply { width -= windowTexture.regionWidth.toFloat() }
    }

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

    private fun handleInsideArmorCell(action: MouseInputAction, armorCell: Int) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow
        val selectedItem = window.selectedItem

        if (selectedItem != null &&
            !selectedItem.item.isNone() &&
            !mobController.player.wearingArmor.getCellType(armorCell).isInstance(selectedItem.item) &&
            selectedItem.amount != 0
        ) {
            return
        }

        handleInsidePlaceableCell(action, mobController.player.wearingArmor.items, window, armorCell)

        updateCraftResult(window)
    }

    private fun handleInsideCraftResult(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        handleInsideCraftResultCell(action, window.craftResultList, window, 0)

        updateCraftResult(window)
    }

    private fun handleInsideRecipeBook(action: MouseInputAction) {
        if (!action.actionKey.touchUp) {
            return
        }

        val window = gameWindowsManager.currentWindow as? AbstractInventoryWindowWithCraftGrid ?: return

        val bookRect = getRecipeBookRect()

        val xOnBook = action.screenX - bookRect.x
        val yOnBook = action.screenY - bookRect.y

        val xOnGrid = xOnBook - GameWindowsConfigs.RecipeBook.gridX
        val yOnGrid = yOnBook - GameWindowsConfigs.RecipeBook.gridY

        val isInsideGrid = with(GameWindowsConfigs.RecipeBook) {
            xOnGrid >= 0f &&
                yOnGrid >= 0f &&
                xOnGrid <= gridWidth * cellSize &&
                yOnGrid <= gridHeight * cellSize
        }

        val isInsidePrev = xOnBook > GameWindowsConfigs.RecipeBook.prevX &&
            xOnBook < GameWindowsConfigs.RecipeBook.prevX + 12f &&
            yOnBook > GameWindowsConfigs.RecipeBook.prevY

        val isInsideNext = xOnBook > GameWindowsConfigs.RecipeBook.nextX &&
            xOnBook < GameWindowsConfigs.RecipeBook.nextX + 12f &&
            yOnBook > GameWindowsConfigs.RecipeBook.nextY

        window.selectedRecipe = if (isInsideGrid) {
            val newSelection = with(GameWindowsConfigs.RecipeBook) {
                MathUtils.floor(xOnGrid / cellSize) + MathUtils.floor(yOnGrid / cellSize) * gridWidth
            }
            if (newSelection == window.selectedRecipe) {
                window.tryFillPhantomRecipe(playerAdapter, dropQueue, mobController.player.inventory, itemsRepository)
            } else {
                window.clearCrafting(playerAdapter, dropQueue, itemsRepository)
            }
            updateCraftResult(window)
            newSelection
        } else {
            -1
        }

        if (isInsidePrev || isInsideNext) {
            val pages = window.getAvailableCraftingRecipes(itemsRepository).count() /
                GameWindowsConfigs.RecipeBook.pageSize + 1

            val newPage = if (isInsidePrev) {
                pages + window.recipeBookPage - 1
            } else {
                window.recipeBookPage + 1
            } % pages

            window.recipeBookPage = newPage
        }
    }

    private fun toggleRecipeBook(window: SurvivalInventoryWindow) {
        window.recipeBookActive = !window.recipeBookActive
        window.selectedRecipe = -1
    }

    override fun handle(action: MouseInputAction) {
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        val xOnWindow =
            action.screenX - (
                gameContextRepository.getCameraContext().viewport.width / 2 -
                    if (!window.recipeBookActive) windowTexture.regionWidth / 2 else 0
                )
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

        val armorCell = (yOnWindow - GameWindowsConfigs.Survival.armorGridOffsetY) /
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

        val isInsideArmorGrid = Rectangle(
            GameWindowsConfigs.Survival.armorGridOffsetX,
            GameWindowsConfigs.Survival.armorGridOffsetY,
            GameWindowsConfigs.Survival.itemsGridColWidth,
            GameWindowsConfigs.Survival.itemsGridRowHeight * mobController.player.wearingArmor.items.size,
        ).contains(xOnWindow, yOnWindow)

        val isInsideRecipeButton = action.actionKey.touchUp && Rectangle(
            GameWindowsConfigs.Survival.recipeButtonX,
            GameWindowsConfigs.Survival.recipeButtonY,
            GameWindowsConfigs.Survival.recipeButtonWidth,
            GameWindowsConfigs.Survival.recipeButtonHeight,
        ).contains(xOnWindow, yOnWindow)

        val isInsideRecipeBook = window.recipeBookActive &&
            getRecipeBookRect()
                .contains(action.screenX, action.screenY)

        if (isInsideInventoryGrid) {
            handleInsideInventoryGrid(action, xOnGrid.toInt(), yOnGrid.toInt())
        } else if (isInsideCraftGrid) {
            handleInsideCraft(action, xOnCraft.toInt(), yOnCraft.toInt())
        } else if (isInsideCraftResult) {
            handleInsideCraftResult(action)
        } else if (isInsideArmorGrid) {
            handleInsideArmorCell(action, armorCell.toInt())
        } else if (isInsideRecipeButton) {
            toggleRecipeBook(window)
        } else if (isInsideRecipeBook) {
            handleInsideRecipeBook(action)
        }
    }
}
