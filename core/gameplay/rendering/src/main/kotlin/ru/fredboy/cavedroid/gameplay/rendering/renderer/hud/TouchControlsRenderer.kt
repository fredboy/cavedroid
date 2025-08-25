package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTouchButtonsUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class TouchControlsRenderer @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getTouchButtons: GetTouchButtonsUseCase,
) : IHudRenderer {

    override val renderLayer get() = RENDER_LAYER

    private val shadeTexture get() = textureRegions[SHADE_KEY]

    private val joyBackground = Sprite(textureRegions["joy_background"])
    private val joyStick = Sprite(textureRegions["joy_stick"])

    private fun drawJoystick(spriteBatch: SpriteBatch) {
        val joystick = gameContextRepository.getJoystick().takeIf { it.active } ?: return

        spriteBatch.drawSprite(
            sprite = joyBackground,
            x = joystick.centerX - Joystick.RADIUS,
            y = joystick.centerY - Joystick.RADIUS,
            width = Joystick.SIZE,
            height = Joystick.SIZE,
        )

        spriteBatch.drawSprite(
            sprite = joyStick,
            x = joystick.activeX - Joystick.STICK_SIZE / 2,
            y = joystick.activeY - Joystick.STICK_SIZE / 2,
            width = Joystick.STICK_SIZE,
            height = Joystick.STICK_SIZE,
        )
    }

    private val TouchButton.rectangleOnScreen
        get() = Rectangle(
            /* x = */ if (rectangle.x < 0f) {
                applicationContextRepository.getWidth() + rectangle.x
            } else {
                rectangle.x
            },
            /* y = */ if (rectangle.y < 0f) {
                applicationContextRepository.getHeight() + rectangle.y
            } else {
                rectangle.y
            },
            /* width = */ rectangle.width,
            /* height = */ rectangle.height,
        )

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        if (!applicationContextRepository.isTouch() || gameWindowsManager.currentWindowType != GameWindowType.NONE) {
            return
        }

        val touchControlsMap = getTouchButtons()

        touchControlsMap.forEach { (key, value) ->
            val touchKey = value.rectangleOnScreen
            spriteBatch.draw(
                /* region = */ textureRegions[key],
                /* x = */ touchKey.x,
                /* y = */ touchKey.y,
                /* width = */ touchKey.width,
                /* height = */ touchKey.height,
            )
        }

        drawJoystick(spriteBatch)
    }

    companion object {
        private const val RENDER_LAYER = 100700

        private const val SHADE_KEY = "shade"
    }
}
