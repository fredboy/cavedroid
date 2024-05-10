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
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.ChestInventoryWindow
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.SurvivalInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class SelectChestInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
    private val dropController: DropController,
) : IGameInputHandler<MouseInputAction> {

    private val chestWindowTexture get() = requireNotNull(Assets.textureRegions["chest"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.CHEST &&
                isInsideWindow(action, chestWindowTexture) &&
                (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Right || action.actionKey is MouseInputActionKey.Screen)
                && (action.actionKey.touchUp || action.actionKey is MouseInputActionKey.Screen)
    }

    private fun handleInsideContentGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as ChestInventoryWindow

        val itemIndex = ((xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Chest.contentsInRow))

        if (action.actionKey is MouseInputActionKey.Screen) {
            if (!action.actionKey.touchUp) {
                window.onLeftCLick(window.chest.items as MutableList<InventoryItem?>, gameItemsHolder, itemIndex, action.actionKey.pointer)
            } else {
                if (action.actionKey.pointer == window.selectItemPointer) {
                    window.onLeftCLick(window.chest.items  as MutableList<InventoryItem?>, gameItemsHolder, itemIndex, action.actionKey.pointer)
                } else {
                    window.onRightClick(window.chest.items  as MutableList<InventoryItem?>, itemIndex)
                }
            }
        } else if (action.actionKey is MouseInputActionKey.Left) {
            window.onLeftCLick(window.chest.items  as MutableList<InventoryItem?>, gameItemsHolder, itemIndex)
        } else {
            window.onRightClick(window.chest.items as MutableList<InventoryItem?>, itemIndex)
        }

        Gdx.app.debug(
            TAG,
            "selected item: ${window.selectedItem?.item?.params?.key ?: "null"}; index $itemIndex, grid ($xOnGrid;$yOnGrid)"
        )
    }

    private fun handleInsideInventoryGrid(action: MouseInputAction, xOnGrid: Int, yOnGrid: Int) {
        val window = gameWindowsManager.currentWindow as ChestInventoryWindow

        var itemIndex = ((xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Chest.itemsInRow))
        itemIndex += GameWindowsConfigs.Chest.hotbarCells

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

    override fun handle(action: MouseInputAction) {
        val chestTexture = chestWindowTexture

        val xOnWindow = action.screenX - (action.cameraViewport.width / 2 - chestTexture.regionWidth / 2)
        val yOnWindow = action.screenY - (action.cameraViewport.height / 2 - chestTexture.regionHeight / 2)

        val xOnGrid = (xOnWindow - GameWindowsConfigs.Chest.itemsGridMarginLeft) /
                GameWindowsConfigs.Chest.itemsGridColWidth
        val yOnGrid = (yOnWindow - GameWindowsConfigs.Chest.itemsGridMarginTop) /
                GameWindowsConfigs.Chest.itemsGridRowHeight

        val xOnContent = (xOnWindow - GameWindowsConfigs.Chest.contentsMarginLeft) /
                GameWindowsConfigs.Chest.itemsGridColWidth
        val yOnContent = (yOnWindow - GameWindowsConfigs.Chest.contentsMarginTop) /
                GameWindowsConfigs.Chest.itemsGridRowHeight

        val isInsideInventoryGrid = xOnGrid >= 0 && xOnGrid < GameWindowsConfigs.Chest.itemsInRow &&
                yOnGrid >= 0 && yOnGrid < GameWindowsConfigs.Chest.itemsInCol

        val isInsideContentGrid = xOnContent >= 0 && xOnContent < GameWindowsConfigs.Chest.contentsInRow &&
                yOnContent >= 0 && yOnContent < GameWindowsConfigs.Chest.contentsInCol


        if (isInsideInventoryGrid) {
            handleInsideInventoryGrid(action, xOnGrid.toInt(), yOnGrid.toInt())
        } else if (isInsideContentGrid) {
            handleInsideContentGrid(action, xOnContent.toInt(), yOnContent.toInt())
        }
    }

    companion object {
        private const val TAG = "SelectChestInventoryItemMouseInputHandler"

    }
}