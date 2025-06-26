package ru.fredboy.cavedroid.ux.controls.input.action

import ru.fredboy.cavedroid.ux.controls.input.action.keys.KeyboardInputActionKey

data class KeyboardInputAction(
    val actionKey: KeyboardInputActionKey,
    val isKeyDown: Boolean,
) : IGameInputAction