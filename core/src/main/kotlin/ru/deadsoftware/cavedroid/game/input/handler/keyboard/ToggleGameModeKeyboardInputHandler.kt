package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class ToggleGameModeKeyboardInputHandler @Inject constructor(
    private val mobsController: MobsController
) : IKeyboardInputHandler {


    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.SwitchGameMode && action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        if (mobsController.player.gameMode == 1) {
            mobsController.player.gameMode = 0
        } else if (mobsController.player.gameMode == 0) {
            mobsController.player.gameMode = 1
        }
    }


}