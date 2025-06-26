package ru.fredboy.cavedroid.ux.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.GameConfigurationRepository
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.ux.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class GoLeftKeyboardInputHandler @Inject constructor(
    private val gameConfigurationRepository: GameConfigurationRepository,
    private val mobController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        val isTouch = gameConfigurationRepository.isTouch()
        return action.actionKey is KeyboardInputActionKey.Left &&
                (mobController.player.controlMode == Player.ControlMode.WALK || !isTouch) &&
                (mobController.player.controlMode == Player.ControlMode.WALK || !isTouch)
    }

    override fun handle(action: KeyboardInputAction) {
        if (action.isKeyDown) {
            mobController.player.velocity.x = -mobController.player.speed
            mobController.player.direction = Direction.LEFT
        } else {
            mobController.player.velocity.x = 0f
        }
    }
}