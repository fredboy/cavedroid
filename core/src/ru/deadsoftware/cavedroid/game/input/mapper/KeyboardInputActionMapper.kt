package ru.deadsoftware.cavedroid.game.input.mapper

import com.badlogic.gdx.Input
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import javax.inject.Inject

@GameScope
class KeyboardInputActionMapper @Inject constructor() {

    fun map(key: Int, isKeyDown: Boolean): KeyboardInputAction? {
        val actionKey = when (key) {
            Input.Keys.A, Input.Keys.LEFT -> KeyboardInputActionKey.Left
            Input.Keys.D, Input.Keys.RIGHT -> KeyboardInputActionKey.Right
            Input.Keys.W, Input.Keys.SPACE -> KeyboardInputActionKey.Jump
            Input.Keys.S -> KeyboardInputActionKey.Down

            Input.Keys.E -> KeyboardInputActionKey.OpenInventory
            Input.Keys.ALT_LEFT -> KeyboardInputActionKey.SwitchControlsMode

            Input.Keys.ESCAPE, Input.Keys.BACK -> KeyboardInputActionKey.Pause

            Input.Keys.F1 -> KeyboardInputActionKey.ShowDebug
            Input.Keys.GRAVE -> KeyboardInputActionKey.SwitchGameMode
            Input.Keys.M -> KeyboardInputActionKey.ShowMap

            else -> null
        }

        return actionKey?.let { KeyboardInputAction(it, isKeyDown) }
    }

}