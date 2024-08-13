package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.drop.model.Drop
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class DropItemKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val dropController: DropController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.DropItem &&
                action.isKeyDown && gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE &&
                !mobController.player.inventory.activeItem.item.isNone()
    }

    private fun createDrop(item: Item, playerX: Float, playerY: Float, amount: Int) {
        dropController.addDrop(
            /* x = */ playerX + ((DROP_DISTANCE - Drop.DROP_SIZE / 2) * mobController.player.direction.basis),
            /* y = */ playerY,
            /* item = */ item,
            /* count = */ amount
        )
    }

    override fun handle(action: KeyboardInputAction) {
        val player = mobController.player
        val currentItem = player.inventory.activeItem
        val dropAmount =  if (currentItem.item.isTool()) currentItem.amount else 1

        createDrop(currentItem.item, player.x, player.y, dropAmount)
        player.inventory.decreaseCurrentItemAmount(dropAmount)
    }

    companion object {
        const val DROP_DISTANCE = 20f
    }
}