package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class MoveCursorControlsModeKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobController,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return mainConfig.isTouch &&
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