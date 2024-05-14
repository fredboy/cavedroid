package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.KeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import javax.inject.Inject

@GameScope
@KeyboardInputHandler
class ToggleControlsModeKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.SwitchControlsMode && !action.isKeyDown
                && mainConfig.isTouch
    }

    override fun handle(action: KeyboardInputAction) {
        if (mobsController.player.controlMode == Player.ControlMode.WALK) {
            mobsController.player.controlMode = Player.ControlMode.CURSOR
        } else {
            mobsController.player.controlMode = Player.ControlMode.WALK
        }
    }

}