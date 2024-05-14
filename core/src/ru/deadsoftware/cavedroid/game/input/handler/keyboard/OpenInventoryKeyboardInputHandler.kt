package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.KeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import javax.inject.Inject

@GameScope
@KeyboardInputHandler
class OpenInventoryKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.OpenInventory &&
                !action.isKeyDown && gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE
    }

    override fun handle(action: KeyboardInputAction) {
        gameWindowsManager.openInventory()
    }
}