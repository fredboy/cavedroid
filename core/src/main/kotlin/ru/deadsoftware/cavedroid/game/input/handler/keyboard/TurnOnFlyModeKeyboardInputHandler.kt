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
class TurnOnFlyModeKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return mobsController.player.gameMode == 1 && action.actionKey is KeyboardInputActionKey.Up &&
                !mobsController.player.swim &&
                !mobsController.player.isFlyMode && !mobsController.player.canJump && action.isKeyDown &&
                (mobsController.player.controlMode == Player.ControlMode.WALK || !mainConfig.isTouch)
    }

    override fun handle(action: KeyboardInputAction) {
        mobsController.player.isFlyMode = true
        mobsController.player.velocity.y = 0f
    }

}