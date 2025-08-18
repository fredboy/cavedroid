package ru.fredboy.cavedroid.gameplay.controls.input.action

import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey

data class MouseInputAction(
    val screenX: Float,
    val screenY: Float,
    val actionKey: MouseInputActionKey,
) : IGameInputAction
