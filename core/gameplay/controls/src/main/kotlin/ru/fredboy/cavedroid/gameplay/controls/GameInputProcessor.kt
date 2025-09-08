package ru.fredboy.cavedroid.gameplay.controls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifFalse
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import ru.fredboy.cavedroid.domain.assets.usecase.GetTouchButtonsUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse.MouseCursorInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.mapper.KeyboardInputActionMapper
import ru.fredboy.cavedroid.gameplay.controls.input.mapper.MouseInputActionMapper
import javax.inject.Inject
import kotlin.math.abs

@GameScope
class GameInputProcessor @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val getTouchButtonsUseCase: GetTouchButtonsUseCase,
    private val mouseCursorInputHandler: MouseCursorInputHandler,
    private val mouseInputActionMapper: MouseInputActionMapper,
    private val keyboardInputActionMapper: KeyboardInputActionMapper,
    private val mouseInputHandlers: Set<@JvmSuppressWildcards IMouseInputHandler>,
    private val keyboardInputHandlers: Set<@JvmSuppressWildcards IKeyboardInputHandler>,
) : InputProcessor {

    private var touchDownX = 0f
    private var touchDownY = 0f

    private val pointerToTouchedKey = mutableMapOf<Int, TouchButton>()

    override fun keyDown(keycode: Int): Boolean = handleKeyboardAction(keycode, true)

    override fun keyUp(keycode: Int): Boolean = handleKeyboardAction(keycode, false)

    override fun keyTyped(p0: Char): Boolean = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val (touchX, touchY) = gameContextRepository.getCameraContext().getViewportCoordinates(screenX, screenY)

        touchDownX = touchX
        touchDownY = touchY

        if (applicationContextRepository.isTouch()) {
            val touchedKey = getTouchedKey(touchX, touchY)

            pointerToTouchedKey[pointer] = touchedKey

            return if (!touchedKey.isMouse) {
                keyDown(touchedKey.code)
            } else {
                onMouseActionEvent(
                    mouseX = screenX,
                    mouseY = screenY,
                    button = touchedKey.code,
                    touchUp = false,
                    pointer = pointer,
                )
            }
        }

        return onMouseActionEvent(screenX, screenY, button, false, pointer)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val (touchX, touchY) = gameContextRepository.getCameraContext().getViewportCoordinates(screenX, screenY)

        val joy = gameContextRepository.getJoystick()

        if (applicationContextRepository.isTouch()) {
            if (joy.active && joy.pointer == pointer) {
                return onMouseActionEvent(
                    mouseX = screenX,
                    mouseY = screenY,
                    button = nullButton.code,
                    touchUp = true,
                    pointer = pointer,
                )
            }

            val touchedKey: TouchButton = pointerToTouchedKey[pointer] ?: getTouchedKey(touchX, touchY)

            return (touchedKey.isMouse).ifFalse {
                keyUp(touchedKey.code).takeIf { it }
            } ?: onMouseActionEvent(
                mouseX = screenX,
                mouseY = screenY,
                button = touchedKey.takeIf { it.isMouse }?.code ?: nullButton.code,
                touchUp = true,
                pointer = pointer,
            )
        }

        return onMouseActionEvent(
            mouseX = screenX,
            mouseY = screenY,
            button = button,
            touchUp = true,
            pointer = pointer,
        )
    }

    override fun touchCancelled(p0: Int, p1: Int, p2: Int, p3: Int): Boolean = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val (touchX, touchY) = gameContextRepository.getCameraContext().getViewportCoordinates(screenX, screenY)

        if (abs(touchX - touchDownX) < 16 && abs(touchY - touchDownY) < DRAG_THRESHOLD) {
            return false
        }

        val touchedKey = getTouchedKey(touchX, touchY)
        if (pointerToTouchedKey[pointer]?.code != touchedKey.code) {
            pointerToTouchedKey.remove(pointer)?.let { key ->
                return if (!key.isMouse) {
                    keyUp(key.code)
                } else {
                    onMouseActionEvent(
                        mouseX = screenX,
                        mouseY = screenY,
                        button = key.code,
                        touchUp = true,
                        pointer = pointer,
                    )
                }
            }
        }

        val action = mouseInputActionMapper.mapDragged(
            mouseX = screenX.toFloat(),
            mouseY = screenY.toFloat(),
            pointer = pointer,
        )

        return handleMouseAction(action)
    }

    override fun mouseMoved(p0: Int, p1: Int): Boolean = false

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        val action: MouseInputAction? = mouseInputActionMapper
            .mapScrolled(
                mouseX = Gdx.input.x.toFloat(),
                mouseY = Gdx.input.y.toFloat(),
                amountX = amountX,
                amountY = amountY,
            )
        return handleMouseAction(action)
    }

    @Suppress("unused")
    fun update(delta: Float) {
        handleMousePosition()

        gameContextRepository.getJoystick()
            .takeIf { joystick -> joystick.active }
            ?.let { joystick ->
                joystick.updateState(
                    touchX = applicationContextRepository.getWidth() / Gdx.graphics.width * Gdx.input.getX(joystick.pointer),
                    touchY = applicationContextRepository.getHeight() / Gdx.graphics.height * Gdx.input.getY(joystick.pointer),
                )
            }
    }

    private val TouchButton.rectangleOnScreen
        get() = Rectangle(
            /* x = */
            if (rectangle.x < 0f) {
                applicationContextRepository.getWidth() + rectangle.x
            } else {
                rectangle.x
            },
            /* y = */
            if (rectangle.y < 0f) {
                applicationContextRepository.getHeight() + rectangle.y
            } else {
                rectangle.y
            },
            /* width = */ rectangle.width,
            /* height = */ rectangle.height,
        )

    private fun getTouchedKey(touchX: Float, touchY: Float): TouchButton {
        for (entry in getTouchButtonsUseCase().entries) {
            val button = entry.value
            if (button.rectangleOnScreen.contains(touchX, touchY)) {
                return button
            }
        }

        return nullButton
    }

    private fun handleMouseAction(action: MouseInputAction?): Boolean {
        if (action == null) {
            return false
        }

        var anyProcessed = false

        for (handler in mouseInputHandlers) {
            val conditions: Boolean = handler.checkConditions(action)
            if (conditions) {
                anyProcessed = true
                handler.handle(action)
                break
            }
        }

        return anyProcessed
    }

    private fun handleKeyboardAction(keycode: Int, isKeyDown: Boolean): Boolean {
        val action = keyboardInputActionMapper.map(keycode, isKeyDown)

        if (action == null) {
            return false
        }

        var anyProcessed = false

        for (handler in keyboardInputHandlers) {
            val conditions: Boolean = handler.checkConditions(action)
            if (conditions) {
                anyProcessed = true
                handler.handle(action)
                break
            }
        }

        return anyProcessed
    }

    private fun onMouseActionEvent(mouseX: Int, mouseY: Int, button: Int, touchUp: Boolean, pointer: Int): Boolean {
        val action: MouseInputAction? = mouseInputActionMapper.map(
            mouseX = mouseX.toFloat(),
            mouseY = mouseY.toFloat(),
            button = button,
            touchUp = touchUp,
            pointer = pointer,
        )
        return handleMouseAction(action)
    }

    private fun handleMousePosition() {
        if (applicationContextRepository.isTouch()) {
            return
        }
        val cameraContext = gameContextRepository.getCameraContext()

        val screenX = cameraContext.xOnViewport(Gdx.input.x)
        val screenY = cameraContext.yOnViewport(Gdx.input.y)

        val action = MouseInputAction(
            screenX = screenX,
            screenY = screenY,
            actionKey = MouseInputActionKey.None,
        )

        mouseCursorInputHandler.handle(action)
    }

    companion object {
        private const val TAG = "GameInputProcessor"

        private const val DRAG_THRESHOLD = 0.2f

        private val nullButton = TouchButton(Rectangle(), -1, true)
    }
}
