package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler
import ru.deadsoftware.cavedroid.game.input.MouseInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideHotbar
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@MouseInputHandler
class AttackMouseInputHandler @Inject constructor(
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
    private val gameWindowsManager: GameWindowsManager
) : IMouseInputHandler {

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE &&
                !isInsideHotbar(action) &&
                action.actionKey is MouseInputActionKey.Left

    }

    override fun handle(action: MouseInputAction) {
        if (action.actionKey.touchUp) {
            mobsController.player.stopHitting()
        } else {
            mobsController.player.startHitting()
        };
    }
}