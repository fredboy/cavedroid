package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import javax.inject.Inject

@GameScope
class ToggleGameModeKeyboardInputHandler @Inject constructor(
    private val mobsController: MobsController
) : IGameInputHandler<KeyboardInputAction> {


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