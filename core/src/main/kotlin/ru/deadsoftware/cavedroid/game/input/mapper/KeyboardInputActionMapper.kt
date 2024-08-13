package ru.deadsoftware.cavedroid.game.input.mapper

import com.badlogic.gdx.Input
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.common.di.GameScope
import javax.inject.Inject

@GameScope
class KeyboardInputActionMapper @Inject constructor() {

    fun map(key: Int, isKeyDown: Boolean): KeyboardInputAction? {
        val actionKey = when (key) {
            Input.Keys.A, Input.Keys.LEFT -> KeyboardInputActionKey.Left
            Input.Keys.D, Input.Keys.RIGHT -> KeyboardInputActionKey.Right
            Input.Keys.W, Input.Keys.SPACE -> KeyboardInputActionKey.Up
            Input.Keys.S -> KeyboardInputActionKey.Down

            Input.Keys.E -> KeyboardInputActionKey.OpenInventory
            Input.Keys.ALT_LEFT -> KeyboardInputActionKey.SwitchControlsMode

            Input.Keys.ESCAPE, Input.Keys.BACK -> KeyboardInputActionKey.Pause

            Input.Keys.F1 -> KeyboardInputActionKey.ShowDebug
            Input.Keys.GRAVE -> KeyboardInputActionKey.SwitchGameMode
            Input.Keys.M -> KeyboardInputActionKey.ShowMap

            Input.Keys.Q -> KeyboardInputActionKey.DropItem

            Input.Keys.NUM_1 -> KeyboardInputActionKey.SelectHotbarSlot(0)
            Input.Keys.NUM_2 -> KeyboardInputActionKey.SelectHotbarSlot(1)
            Input.Keys.NUM_3 -> KeyboardInputActionKey.SelectHotbarSlot(2)
            Input.Keys.NUM_4 -> KeyboardInputActionKey.SelectHotbarSlot(3)
            Input.Keys.NUM_5 -> KeyboardInputActionKey.SelectHotbarSlot(4)
            Input.Keys.NUM_6 -> KeyboardInputActionKey.SelectHotbarSlot(5)
            Input.Keys.NUM_7 -> KeyboardInputActionKey.SelectHotbarSlot(6)
            Input.Keys.NUM_8 -> KeyboardInputActionKey.SelectHotbarSlot(7)
            Input.Keys.NUM_9 -> KeyboardInputActionKey.SelectHotbarSlot(8)

            else -> null
        }

        return actionKey?.let { KeyboardInputAction(it, isKeyDown) }
    }

}