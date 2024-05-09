package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.DropController
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import javax.inject.Inject

@GameScope
class OpenInventoryKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
) : IGameInputHandler<KeyboardInputAction> {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.OpenInventory &&
                !action.isKeyDown && gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE
    }

    override fun handle(action: KeyboardInputAction) {
        gameWindowsManager.openInventory()
    }
}