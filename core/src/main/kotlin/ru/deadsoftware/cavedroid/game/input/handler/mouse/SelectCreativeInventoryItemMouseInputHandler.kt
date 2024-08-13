package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideWindow
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindMouseInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByIndexUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class SelectCreativeInventoryItemMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getItemByIndexUseCase: GetItemByIndexUseCase,
) : IMouseInputHandler {

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.CREATIVE_INVENTORY &&
                !gameWindowsManager.isDragging &&
                (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
                action.actionKey.touchUp && isInsideWindow(action, creativeInventoryTexture)
    }

    override fun handle(action: MouseInputAction) {
        val creativeTexture = creativeInventoryTexture
        val xOnGrid = (action.screenX - (action.cameraViewport.width / 2 - creativeTexture.regionWidth / 2 +
                GameWindowsConfigs.Creative.itemsGridMarginLeft)) /
                GameWindowsConfigs.Creative.itemsGridColWidth
        val yOnGrid = (action.screenY - (action.cameraViewport.height / 2 - creativeTexture.regionHeight / 2 +
                GameWindowsConfigs.Creative.itemsGridMarginTop)) /
                GameWindowsConfigs.Creative.itemsGridRowHeight

        if (xOnGrid < 0 || xOnGrid >= GameWindowsConfigs.Creative.itemsInRow ||
            yOnGrid < 0 || yOnGrid >= GameWindowsConfigs.Creative.itemsInCol) {
            return
        }

        val itemIndex = (gameWindowsManager.creativeScrollAmount * GameWindowsConfigs.Creative.itemsInRow +
                (xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Creative.itemsInRow))
        val item = getItemByIndexUseCase[itemIndex]
        mobController.player.inventory.addItem(item)
    }

}