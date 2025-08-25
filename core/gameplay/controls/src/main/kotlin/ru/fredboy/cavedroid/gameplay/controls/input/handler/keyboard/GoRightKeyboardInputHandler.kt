package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class GoRightKeyboardInputHandler @Inject constructor(
    private val mobController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Right
    }

    override fun handle(action: KeyboardInputAction) {
        if (action.isKeyDown) {
            mobController.player.controlVector.x = mobController.player.speed
            mobController.player.direction = Direction.RIGHT
        } else {
            mobController.player.controlVector.x = 0f
        }
    }
}
