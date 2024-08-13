package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.model.Player
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class FlyDownKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Down &&
                mobController.player.isFlyMode &&
                (mobController.player.controlMode == Player.ControlMode.WALK || !mainConfig.isTouch)
    }

    override fun handle(action: KeyboardInputAction) {
        if (action.isKeyDown) {
            mobController.player.velocity.y = mobController.player.speed
        } else {
            mobController.player.velocity.y = 0f
        }
    }

}