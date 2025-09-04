package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.isInsideWindow
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class CloseGameWindowMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
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

    override fun checkConditions(action: MouseInputAction): Boolean = gameWindowsManager.currentWindowType != GameWindowType.NONE &&
        (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
        !action.actionKey.touchUp &&
        !isInsideWindow(gameContextRepository, action, getCurrentWindowTexture())

    private fun getCurrentWindowTexture(): TextureRegion = when (val window = gameWindowsManager.currentWindowType) {
        GameWindowType.CREATIVE_INVENTORY -> creativeInventoryTexture
        GameWindowType.SURVIVAL_INVENTORY -> survivalInventoryTexture
        GameWindowType.CRAFTING_TABLE -> craftingInventoryTexture
        GameWindowType.FURNACE -> furnaceInventoryTexture
        GameWindowType.CHEST -> chestInventoryTexture
        else -> throw UnsupportedOperationException("Cant close window ${window.name}")
    }

    override fun handle(action: MouseInputAction) {
        val selectedItem = gameWindowsManager.currentWindow?.selectedItem
        if (selectedItem != null) {
            dropController.addDrop(
                /* x = */ mobController.player.position.x + (2f * mobController.player.direction.basis),
                /* y = */ mobController.player.position.y,
                /* item = */ selectedItem.item,
                /* count = */ selectedItem.amount,
            )
            gameWindowsManager.currentWindow?.selectedItem = null
        }
        gameWindowsManager.closeWindow()
    }
}
