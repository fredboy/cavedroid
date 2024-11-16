package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class SwimUpKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
) : IKeyboardInputHandler {

    private fun checkSwim(): Boolean {
        return gameWorld.getForeMap(mobController.player.mapX, mobController.player.lowerMapY).isFluid()
    }

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Up && action.isKeyDown &&
                !mobController.player.swim &&
                !mobController.player.canJump &&
                checkSwim() && !mobController.player.isFlyMode &&
                (mobController.player.controlMode == Player.ControlMode.WALK || !mainConfig.isTouch)
    }

    override fun handle(action: KeyboardInputAction) {
        mobController.player.swim = true
    }

}