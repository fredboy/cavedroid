package ru.fredboy.cavedroid.ux.controls.input.handler.touch

import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.ux.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.ux.controls.input.isInsideHotbar
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class JoystickInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private var activateTimeMs = 0L
    private var cursorTimeoutMs = 100L

    private var active = false
        set(value) {
            if (!value) {
                resetVelocity()
                if (TimeUtils.timeSinceMillis(activateTimeMs) < 200L &&
                    mobController.player.controlMode != Player.ControlMode.CURSOR
                ) {
                    if (mobController.player.canJump) {
                        mobController.player.jump()
                    } else if (mobController.player.gameMode == 1) {
                        mobController.player.isFlyMode = true
                    }
                }
            } else {
                activateTimeMs = TimeUtils.millis()
            }
            field = value
        }

    private fun resetVelocity() {
        mobController.player.controlVector.x = 0f

        if (mobController.player.isFlyMode) {
            mobController.player.controlVector.y = 0f
        }
    }

    override fun checkConditions(action: MouseInputAction): Boolean = gameWindowsManager.currentWindowType == GameWindowType.NONE &&
        applicationContextRepository.isTouch() &&
        action.actionKey is MouseInputActionKey.Touch &&
        (action.actionKey.pointer == gameContextRepository.getJoystick().pointer || !active) &&
        (
            (action.actionKey is MouseInputActionKey.Dragged) ||
                (action.screenX < action.cameraViewport.width / 2 && !action.actionKey.touchUp || active)
            ) &&
        !(action.actionKey is MouseInputActionKey.Screen && action.isInsideHotbar(textureRegions))

    private fun handleTouchDown(action: MouseInputAction) {
        val key = action.actionKey as MouseInputActionKey.Screen
        gameContextRepository.getJoystick().activate(action.screenX, action.screenY, key.pointer)
        active = true
    }

    private fun handleTouchUp() {
        gameContextRepository.getJoystick().deactivate()
        active = false
        mobController.player.swim = false
    }

    private fun handleCursor() {
        val joystick = gameContextRepository.getJoystick()

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

        val joystick = gameContextRepository.getJoystick()
        val joyVector = joystick.getVelocityVector()

        mobController.player.controlVector.x = joyVector.x

        mobController.player.direction = if (joyVector.x < 0) {
            Direction.LEFT
        } else {
            Direction.RIGHT
        }

        if (mobController.player.isFlyMode) {
            mobController.player.controlVector.y = joyVector.y
        }

        mobController.player.swim = true
    }

    override fun handle(action: MouseInputAction) {
        when (action.actionKey) {
            is MouseInputActionKey.Dragged -> handleDragged()
            else -> {
                if (action.actionKey.touchUp) {
                    handleTouchUp()
                } else {
                    handleTouchDown(action)
                }
            }
        }
    }
}
