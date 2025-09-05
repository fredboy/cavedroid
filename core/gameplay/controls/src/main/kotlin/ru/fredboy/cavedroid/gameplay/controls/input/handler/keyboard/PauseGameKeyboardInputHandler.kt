package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class PauseGameKeyboardInputHandler @Inject constructor(
    private val gameController: ApplicationController,
    private val gameWindowsManager: GameWindowsManager,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.Pause && action.isKeyDown

    override fun handle(action: KeyboardInputAction) {
        if (gameWindowsManager.currentWindowType != GameWindowType.NONE) {
            gameWindowsManager.closeWindow()
            return
        }

        gameController.pauseGame()
    }
}
