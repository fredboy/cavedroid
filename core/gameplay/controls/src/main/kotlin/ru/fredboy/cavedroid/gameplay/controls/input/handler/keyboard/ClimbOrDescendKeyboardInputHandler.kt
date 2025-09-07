package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class ClimbOrDescendKeyboardInputHandler @Inject constructor(
    private val mobController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = (
        action.actionKey is KeyboardInputActionKey.Up ||
            action.actionKey is KeyboardInputActionKey.Down
        ) &&
        !mobController.player.canJump &&
        !mobController.player.isFlyMode

    override fun handle(action: KeyboardInputAction) {
        when (action.actionKey) {
            is KeyboardInputActionKey.Up -> {
                mobController.player.climb = action.isKeyDown && mobController.player.canClimb
            }

            is KeyboardInputActionKey.Down -> {
                mobController.player.descend = action.isKeyDown && mobController.player.canClimb
            }

            else -> {}
        }
    }
}
