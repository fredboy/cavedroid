package ru.deadsoftware.cavedroid.game.input.handler.mouse

import com.badlogic.gdx.utils.Timer
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideHotbar
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class HotbarMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
) : IGameInputHandler<MouseInputAction> {

    private val hotbarTexture get() = requireNotNull(Assets.textureRegions["hotbar"])

    private var buttonHoldTask: Timer.Task? = null

    override fun checkConditions(action: MouseInputAction): Boolean {
        return buttonHoldTask?.isScheduled == true ||
                (action.actionKey is MouseInputActionKey.Left && isInsideHotbar(action) || action.actionKey is MouseInputActionKey.Scroll) &&
                gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE
    }

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun handleHold() {
        buttonHoldTask = null
        gameWindowsManager.openInventory()
    }

    private fun handleDown(action: MouseInputAction) {
        buttonHoldTask = object : Timer.Task() {
            override fun run() {
                handleHold()
            }
        }

        Timer.schedule(buttonHoldTask, TOUCH_HOLD_TIME_SEC)
    }

    private fun handleUp(action: MouseInputAction) {
        mobsController.player.slot =
            ((action.screenX -
                    (action.cameraViewport.width / 2 - hotbarTexture.regionWidth / 2))
                    / HOTBAR_CELL_WIDTH).toInt()
    }

    private fun handleScroll(action: MouseInputAction) {
        if (action.actionKey !is MouseInputActionKey.Scroll) {
            return
        }
        mobsController.player.slot += action.actionKey.amountY.toInt()
        if (mobsController.player.slot < 0) {
            mobsController.player.slot = HOTBAR_ITEMS - 1
        } else if (mobsController.player.slot >= HOTBAR_ITEMS){
            mobsController.player.slot = 0
        }
    }

    override fun handle(action: MouseInputAction) {
        if (buttonHoldTask != null && buttonHoldTask?.isScheduled == true) {
            cancelHold()
        }

        if (action.actionKey !is MouseInputActionKey.Left) {
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
        private const val HOTBAR_ITEMS = 9
    }

}