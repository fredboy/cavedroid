package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class CloseGameWindowKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val dropController: DropController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.OpenInventory &&
                !action.isKeyDown && gameWindowsManager.getCurrentWindow() != GameUiWindow.NONE
    }

    override fun handle(action: KeyboardInputAction) {
        val selectedItem = gameWindowsManager.currentWindow?.selectedItem
        if (selectedItem != null) {
            for (i in 1 .. selectedItem.amount) {
                dropController.addDrop(
                    /* x = */ mobController.player.x + (32f * mobController.player.direction.basis),
                    /* y = */ mobController.player.y,
                    /* item = */ selectedItem
                )
            }
            gameWindowsManager.currentWindow?.selectedItem = null
        }
        gameWindowsManager.closeWindow()
    }
}