package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class TurnOnFlyModeKeyboardInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobsController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = mobsController.player.gameMode.isCreative() &&
        action.actionKey is KeyboardInputActionKey.Up &&
        !mobsController.player.climb &&
        !mobsController.player.isFlyMode &&
        !mobsController.player.canJump &&
        !mobsController.player.canClimb &&
        action.isKeyDown &&
        (mobsController.player.controlMode == Player.ControlMode.WALK || !applicationContextRepository.isTouch())

    override fun handle(action: KeyboardInputAction) {
        mobsController.player.isFlyMode = true
        mobsController.player.velocity.y = 0f
    }
}
