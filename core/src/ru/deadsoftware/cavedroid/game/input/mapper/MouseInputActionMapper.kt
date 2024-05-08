package ru.deadsoftware.cavedroid.game.input.mapper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import javax.inject.Inject

@GameScope
class MouseInputActionMapper @Inject constructor(
    val mainConfig: MainConfig,
) {

    fun map(
        mouseX: Float,
        mouseY: Float,
        cameraViewport: Rectangle,
        button: Int,
        touchUp: Boolean,
        pointer: Int,
    ): MouseInputAction? {
        val actionKey = mapActionKey(button, touchUp, pointer) ?: return null

        return MouseInputAction(
            screenX = getScreenX(mouseX),
            screenY = getScreenY(mouseY),
            actionKey = actionKey,
            cameraViewport = cameraViewport,
        )
    }

    fun mapDragged(
        mouseX: Float,
        mouseY: Float,
        cameraViewport: Rectangle,
        pointer: Int,
    ): MouseInputAction {
        return MouseInputAction(
            screenX = getScreenX(mouseX),
            screenY = getScreenY(mouseY),
            actionKey = MouseInputActionKey.Dragged(pointer),
            cameraViewport = cameraViewport,
        )
    }

    fun mapScrolled(
        mouseX: Float,
        mouseY: Float,
        amountX: Float,
        amountY: Float,
        cameraViewport: Rectangle,
    ): MouseInputAction {
        return MouseInputAction(
            screenX = getScreenX(mouseX),
            screenY = getScreenY(mouseY),
            actionKey = MouseInputActionKey.Scroll(amountX, amountY),
            cameraViewport = cameraViewport,
        )
    }

    private fun mapActionKey(button: Int, touchUp: Boolean, pointer: Int): MouseInputActionKey? {
        return when (button) {
            Input.Buttons.LEFT -> MouseInputActionKey.Left(touchUp)
            Input.Buttons.RIGHT -> MouseInputActionKey.Right(touchUp)
            Input.Buttons.MIDDLE -> MouseInputActionKey.Middle(touchUp)
            -1 -> MouseInputActionKey.Screen(touchUp, pointer)
            else -> null
        }
    }

    private fun getScreenX(mouseX: Float): Float {
        return mouseX * (mainConfig.width / Gdx.graphics.width)
    }

    private fun getScreenY(mouseY: Float): Float {
        return mouseY * (mainConfig.height / Gdx.graphics.height)
    }

}