package ru.fredboy.cavedroid.ux.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.ux.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class MoveCursorControlsModeKeyboardInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val mobsController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return gameContextRepository.isTouch() &&
                mobsController.player.controlMode == Player.ControlMode.CURSOR && action.isKeyDown &&
                (action.actionKey is KeyboardInputActionKey.Left ||
                action.actionKey is KeyboardInputActionKey.Right ||
                        action.actionKey is KeyboardInputActionKey.Up ||
                        action.actionKey is KeyboardInputActionKey.Down)
    }

    override fun handle(action: KeyboardInputAction) {
        val player = mobsController.player

        when (action.actionKey) {
            KeyboardInputActionKey.Left -> player.cursorX--
            KeyboardInputActionKey.Right -> player.cursorX++
            KeyboardInputActionKey.Up -> player.cursorY--
            KeyboardInputActionKey.Down -> player.cursorY++
            else -> return
        }

        mobsController.checkPlayerCursorBounds()
    }
}