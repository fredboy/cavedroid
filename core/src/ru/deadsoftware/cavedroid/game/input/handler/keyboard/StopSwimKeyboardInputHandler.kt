package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class StopSwimKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Up && !action.isKeyDown &&
                mobsController.player.swim &&
                (mobsController.player.controlMode == Player.ControlMode.WALK || !mainConfig.isTouch)
    }

    override fun handle(action: KeyboardInputAction) {
        mobsController.player.swim = false
    }

}