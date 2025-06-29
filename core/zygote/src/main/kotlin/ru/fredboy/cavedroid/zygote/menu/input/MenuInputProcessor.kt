package ru.fredboy.cavedroid.zygote.menu.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.menu.model.MenuButton
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository
import ru.fredboy.cavedroid.zygote.menu.action.IMenuAction
import ru.fredboy.cavedroid.zygote.menu.option.bool.IMenuBooleanOption
import javax.inject.Inject

@MenuScope
class MenuInputProcessor @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val menuButtonRepository: MenuButtonRepository,
    private val menuButtonActions: Map<String, @JvmSuppressWildcards IMenuAction>,
    private val menuButtonBooleanOption: Map<String, @JvmSuppressWildcards IMenuBooleanOption>,
) : InputProcessor {

    override fun touchUp(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ): Boolean {
        val cameraContext = gameContextRepository.getCameraContext() ?: return false

        val (touchX, touchY) = cameraContext.getViewportCoordinates(screenX, screenY)

        menuButtonRepository.getCurrentMenuButtons()?.values?.forEachIndexed { index, button ->
            if (!button.isEnabled) {
                return@forEachIndexed
            }

            // TODO: Fix magic numbers
            val rect = Rectangle(
                /* x = */ gameContextRepository.getWidth() / 2 - 100,
                /* y = */ gameContextRepository.getHeight() / 4 + index * 30,
                /* width = */ 200f,
                /* height = */ 20f
            )

            if (rect.contains(touchX, touchY)) {
                when (button) {
                    is MenuButton.Simple -> {
                        val action = menuButtonActions[button.actionKey] ?: run {
                            Gdx.app.error(TAG, "Menu handler for action '${button.actionKey}' not found")
                            return@forEachIndexed
                        }

                        if (action.canPerform()) {
                            action.perform()
                        } else {
                            Gdx.app.debug(TAG, "Can't perform action ${button.actionKey}")
                        }
                    }

                    is MenuButton.BooleanOption -> {
                        button.optionKeys.forEach { optionKey ->
                            menuButtonBooleanOption[optionKey]?.toggleOption() ?: run {
                                Gdx.app.error(TAG, "Menu option handler for option '$optionKey' not found")
                            }
                        }
                    }
                }

            }
        }

        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ): Boolean {
        return false
    }

    override fun touchCancelled(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return false
    }

    companion object {
        private const val TAG = "MenuInputProcessor"
    }
}