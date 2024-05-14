package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class CloseGameWindowKeyboardInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val mobsController: MobsController,
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
                    /* x = */ mobsController.player.x + (32f * mobsController.player.direction.basis),
                    /* y = */ mobsController.player.y,
                    /* item = */ selectedItem.item
                )
            }
            gameWindowsManager.currentWindow?.selectedItem = null
        }
        gameWindowsManager.closeWindow()
    }
}