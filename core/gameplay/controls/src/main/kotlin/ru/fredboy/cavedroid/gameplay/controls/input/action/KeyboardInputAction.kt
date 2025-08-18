package ru.fredboy.cavedroid.gameplay.controls.input.action

import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey

data class KeyboardInputAction(
    val actionKey: KeyboardInputActionKey,
    val isKeyDown: Boolean,
) : IGameInputAction
