package ru.deadsoftware.cavedroid.game.input.handler.mouse

import com.badlogic.gdx.utils.Timer
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.handler.keyboard.DropItemKeyboardInputHandler.Companion.DROP_DISTANCE
import ru.deadsoftware.cavedroid.game.input.isInsideHotbar
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.objects.drop.Drop
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class HotbarMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val dropController: DropController,
) : IGameInputHandler<MouseInputAction> {

    private val hotbarTexture get() = requireNotNull(Assets.textureRegions["hotbar"])

    private var buttonHoldTask: Timer.Task? = null

    override fun checkConditions(action: MouseInputAction): Boolean {
        return buttonHoldTask?.isScheduled == true ||
                ((action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen)
                        && isInsideHotbar(action)
                        || action.actionKey is MouseInputActionKey.Scroll) &&
                gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE
    }

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun createDrop(item: Item, playerX: Float, playerY: Float, amount: Int) {
        dropController.addDrop(
            /* x = */ playerX + ((DROP_DISTANCE - Drop.DROP_SIZE / 2) * mobsController.player.direction.basis),
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
        val player = mobsController.player
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
        mobsController.player.inventory.activeSlot = getActionSlot(action)
    }

    private fun handleScroll(action: MouseInputAction) {
        if (action.actionKey !is MouseInputActionKey.Scroll) {
            return
        }
        mobsController.player.inventory.activeSlot += action.actionKey.amountY.toInt()
        if (mobsController.player.inventory.activeSlot < 0) {
            mobsController.player.inventory.activeSlot = Player.HOTBAR_SIZE - 1
        } else if (mobsController.player.inventory.activeSlot >= Player.HOTBAR_SIZE) {
            mobsController.player.inventory.activeSlot = 0
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