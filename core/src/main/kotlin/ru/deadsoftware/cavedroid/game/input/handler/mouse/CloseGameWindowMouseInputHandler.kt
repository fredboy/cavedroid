package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindMouseInputHandler
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideWindow
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class CloseGameWindowMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val dropController: DropController,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])
    private val survivalInventoryTexture get() = requireNotNull(textureRegions["survival"])
    private val craftingInventoryTexture get() = requireNotNull(textureRegions["crafting_table"])
    private val furnaceInventoryTexture get() = requireNotNull(textureRegions["furnace"])
    private val chestInventoryTexture get() = requireNotNull(textureRegions["chest"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() != GameUiWindow.NONE &&
                (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
                action.actionKey.touchUp &&
                !isInsideWindow(action, getCurrentWindowTexture())
    }

    private fun getCurrentWindowTexture(): TextureRegion {
        return when (val window = gameWindowsManager.getCurrentWindow()) {
            GameUiWindow.CREATIVE_INVENTORY -> creativeInventoryTexture
            GameUiWindow.SURVIVAL_INVENTORY -> survivalInventoryTexture
            GameUiWindow.CRAFTING_TABLE -> craftingInventoryTexture
            GameUiWindow.FURNACE -> furnaceInventoryTexture
            GameUiWindow.CHEST -> chestInventoryTexture
            else -> throw UnsupportedOperationException("Cant close window ${window.name}")
        }
    }

    override fun handle(action: MouseInputAction) {
        val selectedItem = gameWindowsManager.currentWindow?.selectedItem
        if (selectedItem != null) {
                dropController.addDrop(
                    /* x = */ mobController.player.x + (32f * mobController.player.direction.basis),
                    /* y = */ mobController.player.y,
                    /* item = */ selectedItem.item,
                    /* count = */ selectedItem.amount,
                )
            gameWindowsManager.currentWindow?.selectedItem = null
        }
        gameWindowsManager.closeWindow()
    }

}
