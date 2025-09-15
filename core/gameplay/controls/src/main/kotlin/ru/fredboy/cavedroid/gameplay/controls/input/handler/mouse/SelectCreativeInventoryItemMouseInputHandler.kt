package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByIndexUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class SelectCreativeInventoryItemMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getItemByIndexUseCase: GetItemByIndexUseCase,
) : IMouseInputHandler {

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.currentWindowType == GameWindowType.CREATIVE_INVENTORY &&
            !gameWindowsManager.isDragging &&
            (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
            action.actionKey.touchUp &&
            Rectangle(
                0f,
                0f,
                creativeInventoryTexture.regionWidth.toFloat(),
                creativeInventoryTexture.regionHeight.toFloat(),
            )
                .apply {
                    setCenter(
                        gameContextRepository.getCameraContext().viewport.getCenter(
                            Vector2(),
                        ),
                    )
                }
                .contains(action.screenX, action.screenY)
    }

    override fun handle(action: MouseInputAction) {
        val creativeTexture = creativeInventoryTexture
        val xOnGrid = (
            action.screenX - (
                gameContextRepository.getCameraContext().viewport.width / 2 - creativeTexture.regionWidth / 2 +
                    GameWindowsConfigs.Creative.itemsGridMarginLeft
                )
            ) /
            GameWindowsConfigs.Creative.itemsGridColWidth
        val yOnGrid = (
            action.screenY - (
                gameContextRepository.getCameraContext().viewport.height / 2 - creativeTexture.regionHeight / 2 +
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
