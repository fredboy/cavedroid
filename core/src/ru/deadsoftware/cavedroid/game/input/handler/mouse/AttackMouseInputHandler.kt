package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideHotbar
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class AttackMouseInputHandler @Inject constructor(
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
    private val gameWindowsManager: GameWindowsManager
) : IGameInputHandler<MouseInputAction> {

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