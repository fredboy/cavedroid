package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindMouseInputHandler
import com.badlogic.gdx.utils.Timer
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.handler.keyboard.DropItemKeyboardInputHandler.Companion.DROP_DISTANCE
import ru.deadsoftware.cavedroid.game.input.isInsideHotbar
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.drop.model.Drop
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.model.Player
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class HotbarMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val dropController: DropController,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private val hotbarTexture get() = requireNotNull(textureRegions["hotbar"])

    private var buttonHoldTask: Timer.Task? = null

    override fun checkConditions(action: MouseInputAction): Boolean {
        return buttonHoldTask?.isScheduled == true ||
                ((action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen)
                        && action.isInsideHotbar(textureRegions)
                        || action.actionKey is MouseInputActionKey.Scroll) &&
                gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE
    }

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun createDrop(item: Item, playerX: Float, playerY: Float, amount: Int) {
        dropController.addDrop(
            /* x = */ playerX + ((DROP_DISTANCE - Drop.DROP_SIZE / 2) * mobController.player.direction.basis),
            /* y = */ playerY,
            /* item = */ item,
            /* count = */ amount
        )
    }

    private fun getActionSlot(action: MouseInputAction): Int {
        return ((action.screenX -
                (action.cameraViewport.width / 2 - hotbarTexture.regionWidth / 2))
                / HOTBAR_CELL_WIDTH).toInt()
    }

    private fun handleHold(action: MouseInputAction) {
//        buttonHoldTask = null
//        gameWindowsManager.openInventory()
        val player = mobController.player
        val actionSlot = getActionSlot(action)
        val currentItem = player.inventory.items[actionSlot]
        val dropAmount = if (currentItem.item.isTool()) currentItem.amount else 1

        createDrop(currentItem.item, player.x, player.y, dropAmount)
        player.inventory.decreaseItemAmount(actionSlot, dropAmount)
    }

    private fun handleDown(action: MouseInputAction) {
        buttonHoldTask = object : Timer.Task() {
            override fun run() {
                handleHold(action)
            }
        }

        Timer.schedule(buttonHoldTask, TOUCH_HOLD_TIME_SEC)
    }

    private fun handleUp(action: MouseInputAction) {
        mobController.player.inventory.activeSlot = getActionSlot(action)
    }

    private fun handleScroll(action: MouseInputAction) {
        if (action.actionKey !is MouseInputActionKey.Scroll) {
            return
        }
        mobController.player.inventory.activeSlot += action.actionKey.amountY.toInt()
        if (mobController.player.inventory.activeSlot < 0) {
            mobController.player.inventory.activeSlot = Player.HOTBAR_SIZE - 1
        } else if (mobController.player.inventory.activeSlot >= Player.HOTBAR_SIZE) {
            mobController.player.inventory.activeSlot = 0
        }
    }

    override fun handle(action: MouseInputAction) {
        if (buttonHoldTask != null && buttonHoldTask?.isScheduled == true) {
            cancelHold()
        }

        if (buttonHoldTask != null && buttonHoldTask?.isScheduled != true) {
            buttonHoldTask = null
            return
        }

        if (action.actionKey !is MouseInputActionKey.Left && action.actionKey !is MouseInputActionKey.Screen) {
            if (action.actionKey is MouseInputActionKey.Scroll) {
                handleScroll(action)
            }
            return
        }

        if (action.actionKey.touchUp) {
            handleUp(action)
        } else {
            handleDown(action)
        }
    }

    companion object {
        private const val TOUCH_HOLD_TIME_SEC = 0.5f
        private const val HOTBAR_CELL_WIDTH = 20
    }

}