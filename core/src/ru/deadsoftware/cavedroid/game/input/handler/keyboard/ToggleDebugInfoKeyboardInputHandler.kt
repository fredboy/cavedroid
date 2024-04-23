package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import javax.inject.Inject

@GameScope
class ToggleDebugInfoKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig
) : IGameInputHandler<KeyboardInputAction> {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.ShowDebug && action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        mainConfig.isShowInfo = !mainConfig.isShowInfo
    }
}