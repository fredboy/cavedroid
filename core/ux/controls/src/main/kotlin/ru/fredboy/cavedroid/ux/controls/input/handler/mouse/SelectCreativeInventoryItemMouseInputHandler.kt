package ru.fredboy.cavedroid.ux.controls.input.handler.mouse

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByIndexUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.ux.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.ux.controls.input.isInsideWindow
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

    override fun checkConditions(action: MouseInputAction): Boolean = gameWindowsManager.currentWindowType == GameWindowType.CREATIVE_INVENTORY &&
        !gameWindowsManager.isDragging &&
        (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
        action.actionKey.touchUp &&
        isInsideWindow(action, creativeInventoryTexture)

    override fun handle(action: MouseInputAction) {
        val creativeTexture = creativeInventoryTexture
        val xOnGrid = (
            action.screenX - (
                action.cameraViewport.width / 2 - creativeTexture.regionWidth / 2 +
                    GameWindowsConfigs.Creative.itemsGridMarginLeft
                )
            ) /
            GameWindowsConfigs.Creative.itemsGridColWidth
        val yOnGrid = (
            action.screenY - (
                action.cameraViewport.height / 2 - creativeTexture.regionHeight / 2 +
                    GameWindowsConfigs.Creative.itemsGridMarginTop
                )
            ) /
            GameWindowsConfigs.Creative.itemsGridRowHeight

        if (xOnGrid < 0 ||
            xOnGrid >= GameWindowsConfigs.Creative.itemsInRow ||
            yOnGrid < 0 ||
            yOnGrid >= GameWindowsConfigs.Creative.itemsInCol
        ) {
            return
        }

        val itemIndex = (
            gameWindowsManager.creativeScrollAmount * GameWindowsConfigs.Creative.itemsInRow +
                (xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Creative.itemsInRow)
            )
        val item = getItemByIndexUseCase[itemIndex]
        mobController.player.inventory.addItem(item)
    }
}
