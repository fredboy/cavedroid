package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
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
class AttackMouseInputHandler @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val gameContextRepository: GameContextRepository,
) : IMouseInputHandler {

    override fun checkConditions(action: MouseInputAction): Boolean = gameWindowsManager.currentWindowType == GameWindowType.NONE &&
        !action.isInsideHotbar(gameContextRepository, textureRegions) &&
        action.actionKey is MouseInputActionKey.Left

    override fun handle(action: MouseInputAction) {
        if (action.actionKey.touchUp) {
            mobController.player.stopHitting()
        } else {
            val worldX = action.screenX.meters + gameContextRepository.getCameraContext().visibleWorld.x
            val worldY = action.screenY.meters + gameContextRepository.getCameraContext().visibleWorld.y

            val mob = mobController.mobs.firstOrNull { mob ->
                mob.hitbox.contains(worldX, worldY) &&
                    mobController.player.position.cpy().sub(mob.position).len() <= MOB_HIT_RANGE
            }

            if (mob != null) {
                mobController.player.hitMob(mob)
            } else {
                mobController.player.startHitting()
            }
        }
    }

    companion object {
        private const val MOB_HIT_RANGE = 3f
    }
}
