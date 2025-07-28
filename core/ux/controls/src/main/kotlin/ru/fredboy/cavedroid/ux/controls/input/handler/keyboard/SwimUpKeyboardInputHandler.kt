package ru.fredboy.cavedroid.ux.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class SwimUpKeyboardInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
) : IKeyboardInputHandler {

    private fun checkSwim(): Boolean = gameWorld.getForeMap(mobController.player.mapX, mobController.player.lowerMapY).isFluid()

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.Up &&
        action.isKeyDown &&
        !mobController.player.swim &&
        mobController.player.canSwim &&
        !mobController.player.canJump &&
        checkSwim() &&
        !mobController.player.isFlyMode &&
        (mobController.player.controlMode == Player.ControlMode.WALK || !applicationContextRepository.isTouch())

    override fun handle(action: KeyboardInputAction) {
        mobController.player.swim = true
    }
}
