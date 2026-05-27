package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class JumpKeyboardInputHandler @Inject constructor(
    private val mobController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Up &&
            mobController.player.canJump &&
            !mobController.player.isFlyMode &&
            action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        val player = mobController.player
        player.jump()
        if (player.canClimb) {
            player.tryClimb = true
        } else {
            player.addExhaustion(
                if (player.isSprinting) Player.EXHAUSTION_PER_SPRINT_JUMP else Player.EXHAUSTION_PER_JUMP,
            )
        }
    }
}
