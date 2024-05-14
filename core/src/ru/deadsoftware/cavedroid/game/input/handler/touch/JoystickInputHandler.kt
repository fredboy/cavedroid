package ru.deadsoftware.cavedroid.game.input.handler.touch

import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindMouseInputHandler
import com.badlogic.gdx.utils.TimeUtils
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.*
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.Mob
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class JoystickInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameWorld: GameWorld,
) : IMouseInputHandler {

    private var activateTimeMs = 0L
    private var cursorTimeoutMs = 100L

    private var active = false
        set(value) {
            if (!value) {
                resetVelocity()
                if (TimeUtils.timeSinceMillis(activateTimeMs) < 200L &&
                    mobsController.player.controlMode != Player.ControlMode.CURSOR) {
                    mobsController.player.jump()
                }
            } else {
                activateTimeMs = TimeUtils.millis()
            }
            field = value
        }

    private fun resetVelocity() {
        mobsController.player.velocity.x = 0f

        if (mobsController.player.isFlyMode) {
            mobsController.player.velocity.y = 0f
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
                !(action.actionKey is MouseInputActionKey.Screen && isInsideHotbar(action))

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

        val pastCursorX = mobsController.player.cursorX
        val pastCursorY = mobsController.player.cursorY

        if (Math.abs(joystick.activeX - joystick.centerX) >= Joystick.RADIUS / 2) {
            mobsController.player.cursorX += if (joystick.activeX > joystick.centerX) 1 else -1
            cursorTimeoutMs = TimeUtils.millis()
        }

        if (Math.abs(joystick.activeY - joystick.centerY) >= Joystick.RADIUS / 2) {
            mobsController.player.cursorY += if (joystick.activeY > joystick.centerY) 1 else -1
            cursorTimeoutMs = TimeUtils.millis()
        }

        mobsController.player.checkCursorBounds(gameWorld)

        if (mobsController.player.cursorX != pastCursorX || mobsController.player.cursorY != pastCursorY) {
            mobsController.player.blockDamage = 0f
        }
    }

    private fun handleDragged() {
        if (!active) {
            return
        }

        if (mobsController.player.controlMode == Player.ControlMode.CURSOR) {
            handleCursor()
            return
        }

        val joystick = mainConfig.joystick ?: return
        val joyVector = joystick.getVelocityVector()

        if (mobsController.player.isFlyMode) {
            joyVector.scl(2f);
        }

        mobsController.player.velocity.x = joyVector.x

        mobsController.player.setDir(
            if (joyVector.x < 0) {
                Mob.Direction.LEFT
            } else {
                Mob.Direction.RIGHT
            }
        )

        if (mobsController.player.isFlyMode) {
            mobsController.player.velocity.y = joyVector.y
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