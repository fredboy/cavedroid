package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class DropItemKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val playerAdapter: PlayerAdapter,
    private val dropController: DropController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.DropItem &&
        action.isKeyDown &&
        gameWindowsManager.currentWindowType == GameWindowType.NONE &&
        !playerAdapter.activeItem.item.isNone()

    private fun createDrop(item: Item, playerX: Float, playerY: Float, amount: Int) {
        dropController.addDrop(
            x = playerX + DROP_DISTANCE * playerAdapter.direction.basis,
            y = playerY,
            item = item,
            count = amount,
            initialForce = Vector2(50f * playerAdapter.direction.basis, -50f),
        )
    }

    override fun handle(action: KeyboardInputAction) {
        val currentItem = playerAdapter.activeItem
        val dropAmount = if (currentItem.item.isTool()) currentItem.amount else 1

        createDrop(
            item = currentItem.item,
            playerX = playerAdapter.x,
            playerY = playerAdapter.y - playerAdapter.height / 2,
            amount = dropAmount,
        )
        playerAdapter.decreaseCurrentItemCount(dropAmount)
    }

    companion object {
        const val DROP_DISTANCE = 1.25f
    }
}
