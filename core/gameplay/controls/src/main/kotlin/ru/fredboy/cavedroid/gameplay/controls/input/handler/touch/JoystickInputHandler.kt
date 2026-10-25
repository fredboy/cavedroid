package ru.fredboy.cavedroid.gameplay.controls.input.handler.touch

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
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
    private val vehicles: VehicleController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private var resetting = false
    private var activateTimeMs = 0L
    private var tapCandidate = false

    private var active = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (!value) {
                resetVelocity()
                if (!resetting && tapCandidate && TimeUtils.timeSinceMillis(activateTimeMs) < TAP_TIMEOUT_MS) {
                    if (mobController.player.isMounted) {
                        vehicles.dismount()
                    } else if (mobController.player.canJump) {
                        mobController.player.jump()
                    } else if (mobController.player.gameMode.isCreative()) {
                        mobController.player.isFlyMode = true
                    }
                }
            } else {
                activateTimeMs = TimeUtils.millis()
                tapCandidate = true
            }
            field = value
        }

    private fun resetVelocity() {
        mobController.player.controlVector.x = 0f
        mobController.player.isSprinting = false

        if (mobController.player.isFlyMode || mobController.player.canClimb) {
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
                    ) &&
                    !action.actionKey.touchUp
                )
    }

    private fun handleTouchDown(action: MouseInputAction) {
        val key = action.actionKey as MouseInputActionKey.Screen
        gameContextRepository.getJoystick().activate(action.screenX, action.screenY, key.pointer)
        mobController.player.controlMode = Player.ControlMode.WALK
        active = true
    }

    private fun handleTouchUp(action: MouseInputAction) {
        updateTapCandidate(action)
        gameContextRepository.getJoystick().deactivate()
        mobController.player.controlMode = Player.ControlMode.CURSOR
        active = false
        mobController.player.tryClimb = false
        mobController.player.descend = false
    }

    private fun updateTapCandidate(action: MouseInputAction) {
        val joystick = gameContextRepository.getJoystick()
        if (Vector2.dst2(joystick.centerX, joystick.centerY, action.screenX, action.screenY) > TAP_SLOP * TAP_SLOP) {
            tapCandidate = false
        }
    }

    private fun handleDragged(action: MouseInputAction) {
        if (!active) {
            return
        }

        val joystick = gameContextRepository.getJoystick()
        updateTapCandidate(action)
        joystick.updateState(action.screenX, action.screenY)
        val joyVector = joystick.getVelocityVector()

        mobController.player.controlVector.x = joyVector.x

        if (mobController.player.isFlyMode) {
            mobController.player.controlVector.y = joyVector.y
        } else if (mobController.player.canClimb) {
            mobController.player.tryClimb = joyVector.y < -(mobController.player.params.speed / 4f)
            mobController.player.descend = joyVector.y > (mobController.player.params.speed / 4f)
        }

        mobController.player.isSprinting = joystick.rawDistance > Joystick.SPRINT_DISTANCE
    }

    override fun handle(action: MouseInputAction) {
        when (action.actionKey) {
            is MouseInputActionKey.Dragged -> handleDragged(action)
            else -> {
                if (action.actionKey.touchUp) {
                    handleTouchUp(action)
                } else {
                    handleTouchDown(action)
                }
            }
        }
    }

    override fun reset() {
        resetting = true
        try {
            gameContextRepository.getJoystick().deactivate()
            active = false
        } finally {
            resetting = false
        }
    }

    companion object {
        private const val TAP_TIMEOUT_MS = 200L
        private const val TAP_SLOP = 8f
    }
}
