package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.isInsideHotbar
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
                if (TimeUtils.timeSinceMillis(activateTimeMs) < 200L) {
                    if (mobController.player.canJump) {
                        mobController.player.jump()
                    } else if (mobController.player.gameMode.isCreative()) {
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

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.currentWindowType == GameWindowType.NONE &&
            applicationContextRepository.isTouch() &&
            action.actionKey is MouseInputActionKey.Touch &&
            (action.actionKey.pointer == gameContextRepository.getJoystick().pointer || !active) &&
            (
                (
                    action.actionKey is MouseInputActionKey.Dragged &&
                        active &&
                        action.actionKey.pointer == gameContextRepository.getJoystick().pointer
                    ) ||
                    (
                        action.actionKey is MouseInputActionKey.Screen &&
                            action.screenX < applicationContextRepository.getWidth() / 2f &&
                            !action.actionKey.touchUp ||
                            active
                        )
                ) &&
            !(
                action.actionKey is MouseInputActionKey.Screen &&
                    action.isInsideHotbar(
                        gameContextRepository,
                        textureRegions,
                    )
                )
    }

    private fun handleTouchDown(action: MouseInputAction) {
        val key = action.actionKey as MouseInputActionKey.Screen
        gameContextRepository.getJoystick().activate(action.screenX, action.screenY, key.pointer)
        mobController.player.controlMode = Player.ControlMode.WALK
        active = true
    }

    private fun handleTouchUp() {
        gameContextRepository.getJoystick().deactivate()
        mobController.player.controlMode = Player.ControlMode.CURSOR
        active = false
    }

    private fun handleDragged() {
        if (!active) {
            return
        }

        val joystick = gameContextRepository.getJoystick()
        val joyVector = joystick.getVelocityVector()

        mobController.player.controlVector.x = joyVector.x

        if (mobController.player.isFlyMode) {
            mobController.player.controlVector.y = joyVector.y
        }
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
