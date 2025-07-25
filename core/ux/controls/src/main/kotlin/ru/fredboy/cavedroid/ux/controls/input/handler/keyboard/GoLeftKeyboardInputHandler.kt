package ru.fredboy.cavedroid.ux.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
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
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        val isTouch = applicationContextRepository.isTouch()
        return action.actionKey is KeyboardInputActionKey.Left &&
            (mobController.player.controlMode == Player.ControlMode.WALK || !isTouch) &&
            (mobController.player.controlMode == Player.ControlMode.WALK || !isTouch)
    }

    override fun handle(action: KeyboardInputAction) {
        if (action.isKeyDown) {
            mobController.player.controlVector.x = -mobController.player.speed
            mobController.player.direction = Direction.LEFT
        } else {
            mobController.player.controlVector.x = 0f
        }
    }
}
