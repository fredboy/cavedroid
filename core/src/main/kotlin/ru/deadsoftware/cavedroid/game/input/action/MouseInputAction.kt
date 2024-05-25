package ru.deadsoftware.cavedroid.game.input.action

import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey

data class MouseInputAction(
    val screenX: Float,
    val screenY: Float,
    val actionKey: MouseInputActionKey,
    val cameraViewport: Rectangle
) : IGameInputAction