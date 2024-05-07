package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class SwimUpKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
) : IGameInputHandler<KeyboardInputAction> {

    private fun checkSwim(): Boolean {
        return gameWorld.getForeMap(mobsController.player.mapX, mobsController.player.lowerMapY).isFluid()
    }

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Up && action.isKeyDown &&
                !mobsController.player.swim &&
                !mobsController.player.canJump() &&
                checkSwim() && !mobsController.player.isFlyMode &&
                (mobsController.player.controlMode == Player.ControlMode.WALK || !mainConfig.isTouch)
    }

    override fun handle(action: KeyboardInputAction) {
        mobsController.player.swim = true
    }

}