package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class ToggleGameModeKeyboardInputHandler @Inject constructor(
    private val mobController: MobController
) : IKeyboardInputHandler {


    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.SwitchGameMode && action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        if (mobController.player.gameMode == 1) {
            mobController.player.gameMode = 0
        } else if (mobController.player.gameMode == 0) {
            mobController.player.gameMode = 1
        }
    }


}