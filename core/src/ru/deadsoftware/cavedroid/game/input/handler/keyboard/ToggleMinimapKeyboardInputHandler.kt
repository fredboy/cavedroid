package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.KeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import javax.inject.Inject

@GameScope
@KeyboardInputHandler
class ToggleMinimapKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.ShowMap && action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        mainConfig.isShowMap = !mainConfig.isShowMap
    }
}