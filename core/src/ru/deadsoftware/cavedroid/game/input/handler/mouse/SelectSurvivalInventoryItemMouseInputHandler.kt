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
import ru.deadsoftware.cavedroid.game.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class SelectSurvivalInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IGameInputHandler<MouseInputAction> {

    private val survivalWindowTexture get() = requireNotNull(Assets.textureRegions["survival"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.SURVIVAL_INVENTORY &&
            isInsideWindow(action, survivalWindowTexture) &&
                (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Touch)
                && action.actionKey.touchUp
    }

    override fun handle(action: MouseInputAction) {
        val survivalTexture = survivalWindowTexture
        val xOnGrid = (action.screenX - (action.cameraViewport.width / 2 - survivalTexture.regionWidth / 2 +
                GameWindowsConfigs.Survival.itemsGridMarginLeft)) /
                GameWindowsConfigs.Survival.itemsGridColWidth
        val yOnGrid = (action.screenY - (action.cameraViewport.height / 2 - survivalTexture.regionHeight / 2 +
                GameWindowsConfigs.Survival.itemsGridMarginTop)) /
                GameWindowsConfigs.Survival.itemsGridRowHeight

        if (xOnGrid < 0 || xOnGrid >= GameWindowsConfigs.Survival.itemsInRow ||
            yOnGrid < 0 || yOnGrid > GameWindowsConfigs.Survival.itemsInCol) {
            return
        }

        var itemIndex = ((xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Survival.itemsInRow))
        itemIndex += GameWindowsConfigs.Survival.hotbarCells

        if (itemIndex >= mobsController.player.inventory.size) {
            itemIndex -= mobsController.player.inventory.size
        }

        val item = mobsController.player.inventory[itemIndex]
        mobsController.player.inventory[itemIndex] = gameWindowsManager.selectedItem ?: gameItemsHolder.fallbackItem.toInventoryItem()
        gameWindowsManager.selectedItem = item

        Gdx.app.debug(TAG, "selected item: ${gameWindowsManager.selectedItem?.item?.params?.key ?: "null"}; index $itemIndex, grid ($xOnGrid;$yOnGrid)")
    }

    companion object {
        private const val TAG = "SelectSurvivalInventoryItemMouseInputHandler"

    }
}