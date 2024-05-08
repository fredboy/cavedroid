package ru.deadsoftware.cavedroid.game.input.handler.touch

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.Joystick
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideHotbar
import ru.deadsoftware.cavedroid.game.mobs.Mob
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class JoystickInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameWorld: GameWorld,
) : IGameInputHandler<MouseInputAction> {

    private var activateTimeMs = 0L
    private var cursorTimeoutMs = 100L

    private var active = false
        set(value) {
            if (!value) {
                resetVelocity()
                if (TimeUtils.timeSinceMillis(activateTimeMs) < 100L &&
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

        if (TimeUtils.timeSinceMillis(cursorTimeoutMs) < 200L) {
            return
        }

        if (Math.abs(joystick.activeX - joystick.centerX) >= Joystick.RADIUS / 2) {
            mobsController.player.cursorX += if (joystick.activeX > joystick.centerX) 1 else -1
            cursorTimeoutMs = TimeUtils.millis()
        }

        if (Math.abs(joystick.activeY - joystick.centerY) >= Joystick.RADIUS / 2) {
            mobsController.player.cursorY += if (joystick.activeY > joystick.centerY) 1 else -1
            cursorTimeoutMs = TimeUtils.millis()
        }

        mobsController.player.checkCursorBounds(gameWorld)
    }

    private fun handleDragged() {
        if (mobsController.player.controlMode == Player.ControlMode.CURSOR) {
            handleCursor()
            return
        }

        val joystick = mainConfig.joystick ?: return
        val joyVector = joystick.getVelocityVector()

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