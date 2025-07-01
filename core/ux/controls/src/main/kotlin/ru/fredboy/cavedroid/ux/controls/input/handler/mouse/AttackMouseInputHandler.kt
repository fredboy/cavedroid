package ru.fredboy.cavedroid.ux.controls.input.handler.mouse

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
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
class AttackMouseInputHandler @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    override fun checkConditions(action: MouseInputAction): Boolean = gameWindowsManager.currentWindowType == GameWindowType.NONE &&
        !action.isInsideHotbar(textureRegions) &&
        action.actionKey is MouseInputActionKey.Left

    override fun handle(action: MouseInputAction) {
        if (action.actionKey.touchUp) {
            mobController.player.stopHitting()
        } else {
            mobController.player.startHitting()
        }
    }
}
