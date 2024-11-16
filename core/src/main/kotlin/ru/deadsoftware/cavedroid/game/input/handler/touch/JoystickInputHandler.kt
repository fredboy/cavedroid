package ru.deadsoftware.cavedroid.game.input.handler.touch

import com.badlogic.gdx.utils.TimeUtils
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.*
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindMouseInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class JoystickInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameWorld: GameWorld,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private var activateTimeMs = 0L
    private var cursorTimeoutMs = 100L

    private var active = false
        set(value) {
            if (!value) {
                resetVelocity()
                if (TimeUtils.timeSinceMillis(activateTimeMs) < 200L &&
                    mobController.player.controlMode != Player.ControlMode.CURSOR) {
                    mobController.player.jump()
                }
            } else {
                activateTimeMs = TimeUtils.millis()
            }
            field = value
        }

    private fun resetVelocity() {
        mobController.player.velocity.x = 0f

        if (mobController.player.isFlyMode) {
            mobController.player.velocity.y = 0f
        }
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.NONE &&
                mainConfig.isTouch &&
//                mobsController.player.controlMode == Player.ControlMode.WALK &&
                mainConfig.joystick != null &&
                (action.actionKey is MouseInputActionKey.Touch) &&
                (action.actionKey.pointer == mainConfig.joystick?.pointer || !active) &&
                ((action.actionKey is MouseInputActionKey.Dragged) ||
                        (action.screenX < action.cameraViewport.width / 2 && !action.actionKey.touchUp || active)) &&
                !(action.actionKey is MouseInputActionKey.Screen && action.isInsideHotbar(textureRegions))

    }

    private fun handleTouchDown(action: MouseInputAction) {
        val key = action.actionKey as MouseInputActionKey.Screen
        mainConfig.joystick?.activate(action.screenX, action.screenY, key.pointer) ?: return
        active = true
    }

    private fun handleTouchUp(action: MouseInputAction) {
        mainConfig.joystick?.deactivate()
        active = false
    }

    private fun handleCursor() {
        val joystick = mainConfig.joystick ?: return

        if (TimeUtils.timeSinceMillis(cursorTimeoutMs) < 150L) {
            return
        }

        val pastCursorX = mobController.player.cursorX
        val pastCursorY = mobController.player.cursorY

        if (Math.abs(joystick.activeX - joystick.centerX) >= Joystick.RADIUS / 2) {
            mobController.player.cursorX += if (joystick.activeX > joystick.centerX) 1 else -1
            cursorTimeoutMs = TimeUtils.millis()
        }

        if (Math.abs(joystick.activeY - joystick.centerY) >= Joystick.RADIUS / 2) {
            mobController.player.cursorY += if (joystick.activeY > joystick.centerY) 1 else -1
            cursorTimeoutMs = TimeUtils.millis()
        }

        mobController.checkPlayerCursorBounds()

        if (mobController.player.cursorX != pastCursorX || mobController.player.cursorY != pastCursorY) {
            mobController.player.blockDamage = 0f
        }
    }

    private fun handleDragged() {
        if (!active) {
            return
        }

        if (mobController.player.controlMode == Player.ControlMode.CURSOR) {
            handleCursor()
            return
        }

        val joystick = mainConfig.joystick ?: return
        val joyVector = joystick.getVelocityVector()

        if (mobController.player.isFlyMode) {
            joyVector.scl(2f);
        }

        mobController.player.velocity.x = joyVector.x

        mobController.player.direction = if (joyVector.x < 0) {
            Direction.LEFT
        } else {
            Direction.RIGHT
        }

        if (mobController.player.isFlyMode) {
            mobController.player.velocity.y = joyVector.y
        }
    }

    override fun handle(action: MouseInputAction) {
        when (action.actionKey) {
            is MouseInputActionKey.Dragged -> handleDragged()
            else -> {
                if (action.actionKey.touchUp) {
                    handleTouchUp(action)
                } else {
                    handleTouchDown(action)
                }
            }
        }
    }

}