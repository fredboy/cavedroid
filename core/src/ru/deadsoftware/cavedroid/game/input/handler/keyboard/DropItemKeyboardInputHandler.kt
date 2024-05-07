package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.objects.Drop
import ru.deadsoftware.cavedroid.game.objects.DropController
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import javax.inject.Inject

@GameScope
class DropItemKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
    private val dropController: DropController,
) : IGameInputHandler<KeyboardInputAction> {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.DropItem &&
                action.isKeyDown && gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE &&
                !mobsController.player.inventory.activeItem.item.isNone()
    }

    private fun createDrop(item: Item, playerX: Float, playerY: Float) {
        dropController.addDrop(playerX + ((DROP_DISTANCE - Drop.DROP_SIZE / 2) * mobsController.player.direction.basis), playerY, item)
    }

    override fun handle(action: KeyboardInputAction) {
        val player = mobsController.player
        val currentItem = player.inventory.activeItem

        if (!currentItem.item.isTool()) {
            createDrop(currentItem.item, player.x, player.y)
        } else {
            for (i in 1..currentItem.amount) {
                createDrop(currentItem.item, player.x, player.y)
            }
        }

        player.inventory.decreaseCurrentItemAmount(
            if (currentItem.item.isTool()) {
                currentItem.amount
            } else {
                1
            }
        )
    }

    companion object {
        private const val DROP_DISTANCE = 20f
    }
}