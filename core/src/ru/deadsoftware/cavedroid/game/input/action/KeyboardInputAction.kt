package ru.deadsoftware.cavedroid.game.input.action

import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey

data class KeyboardInputAction(
    val actionKey: KeyboardInputActionKey,
    val isKeyDown: Boolean,
) : IGameInputAction