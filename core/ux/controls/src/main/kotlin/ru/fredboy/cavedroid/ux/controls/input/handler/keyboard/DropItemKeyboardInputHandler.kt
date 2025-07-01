package ru.fredboy.cavedroid.ux.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.ux.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class DropItemKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val dropController: DropController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.DropItem &&
        action.isKeyDown &&
        gameWindowsManager.currentWindowType == GameWindowType.NONE &&
        !mobController.player.activeItem.item.isNone()

    private fun createDrop(item: Item, playerX: Float, playerY: Float, amount: Int) {
        dropController.addDrop(
            /* x = */ playerX + ((DROP_DISTANCE - Drop.DROP_SIZE / 2) * mobController.player.direction.basis),
            /* y = */ playerY,
            /* item = */ item,
            /* count = */ amount,
        )
    }

    override fun handle(action: KeyboardInputAction) {
        val player = mobController.player
        val currentItem = player.activeItem
        val dropAmount = if (currentItem.item.isTool()) currentItem.amount else 1

        createDrop(currentItem.item, player.x, player.y, dropAmount)
        player.decreaseCurrentItemCount(dropAmount)
    }

    companion object {
        const val DROP_DISTANCE = 20f
    }
}
