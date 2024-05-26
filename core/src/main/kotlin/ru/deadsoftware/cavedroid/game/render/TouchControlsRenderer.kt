package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.Joystick
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player.ControlMode
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindRenderer
import ru.deadsoftware.cavedroid.misc.utils.ArrayMapExtensions.component1
import ru.deadsoftware.cavedroid.misc.utils.ArrayMapExtensions.component2
import ru.deadsoftware.cavedroid.misc.utils.drawSprite
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTouchButtonsUseCase
import javax.inject.Inject

@GameScope
@BindRenderer
class TouchControlsRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getTouchButtons: GetTouchButtonsUseCase,
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

    private val shadeTexture get() = textureRegions[SHADE_KEY]

    private val joyBackground = Sprite(textureRegions["joy_background"])
    private val joyStick = Sprite(textureRegions["joy_stick"])

    private fun drawJoystick(spriteBatch: SpriteBatch) {
        val joystick = mainConfig.joystick?.takeIf { it.active } ?: return

        spriteBatch.drawSprite(
            sprite = joyBackground,
            x = joystick.centerX - Joystick.RADIUS,
            y = joystick.centerY - Joystick.RADIUS,
            width = Joystick.SIZE,
            height = Joystick.SIZE
        )

        spriteBatch.drawSprite(
            sprite = joyStick,
            x = joystick.activeX - Joystick.STICK_SIZE / 2,
            y = joystick.activeY - Joystick.STICK_SIZE / 2,
            width = Joystick.STICK_SIZE,
            height = Joystick.STICK_SIZE
        )
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        if (!mainConfig.isTouch || gameWindowsManager.getCurrentWindow() != GameUiWindow.NONE) {
            return
        }

        val touchControlsMap = getTouchButtons()

        touchControlsMap.forEach { (key, value) ->
            val touchKey = value.rectangle
            spriteBatch.draw(
                /* region = */ textureRegions[key],
                /* x = */ touchKey.x,
                /* y = */ touchKey.y,
                /* width = */ touchKey.width,
                /* height = */ touchKey.height
            )
        }

        // FIXME: Add pressed state for buttons
        if (mobsController.player.controlMode == ControlMode.CURSOR) {
            val altKeyRect = touchControlsMap["alt"]?.rectangle ?: return
            spriteBatch.draw(shadeTexture, altKeyRect.x, altKeyRect.y, altKeyRect.width, altKeyRect.height)
        }

        drawJoystick(spriteBatch)
    }

    companion object {
        private const val RENDER_LAYER = 100700

        private const val SHADE_KEY = "shade"
    }

}