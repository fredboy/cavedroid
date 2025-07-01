package ru.fredboy.cavedroid.ux.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.ux.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class OpenInventoryKeyboardInputHandler @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.OpenInventory &&
        !action.isKeyDown &&
        gameWindowsManager.currentWindowType == GameWindowType.NONE

    override fun handle(action: KeyboardInputAction) {
        if (mobController.player.gameMode == 1) {
            gameWindowsManager.openCreativeInventory()
        } else {
            gameWindowsManager.openSurvivalInventory()
        }
    }
}
