package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class ToggleDebugInfoKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.ShowDebug && action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        mainConfig.isShowInfo = !mainConfig.isShowInfo
    }
}