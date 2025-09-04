package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.takeIfTrue
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.placeToBackgroundAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeToForegroundAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.IPlaceBlockAction
import ru.fredboy.cavedroid.gameplay.controls.action.useblock.IUseBlockAction
import ru.fredboy.cavedroid.gameplay.controls.action.useitem.IUseItemAction
import ru.fredboy.cavedroid.gameplay.controls.action.usemob.IUseMobAction
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.isInsideHotbar
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class UseItemMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val mobController: MobController,
    private val useItemActionMap: Map<String, @JvmSuppressWildcards IUseItemAction>,
    private val placeBlockActionMap: Map<String, @JvmSuppressWildcards IPlaceBlockAction>,
    private val useBlockActionMap: Map<String, @JvmSuppressWildcards IUseBlockAction>,
    private val useMobActionMap: Map<String, @JvmSuppressWildcards IUseMobAction>,
    private val gameWindowsManager: GameWindowsManager,
    private val gameWorld: GameWorld,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val applicationContextRepository: ApplicationContextRepository,
) : IMouseInputHandler {

    private var buttonHoldTask: Timer.Task? = null

    override fun checkConditions(action: MouseInputAction): Boolean {
        return !applicationContextRepository.isTouch() &&
            (
                buttonHoldTask?.isScheduled == true ||
                    !action.isInsideHotbar(gameContextRepository, textureRegions) &&
                    gameWindowsManager.currentWindowType == GameWindowType.NONE &&
                    action.actionKey is MouseInputActionKey.Right
                )
    }

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun handleHold() {
        cancelHold()

        val player = mobController.player
        val item = player.activeItem.item
        player.startHitting(false)
        player.stopHitting()

        if (item is Item.Placeable) {
            placeBlockActionMap.placeToBackgroundAction(
                item = item,
                x = player.selectedX,
                y = player.selectedY,
            )
        }
    }

    private fun handleDown() {
        cancelHold()
        buttonHoldTask = object : Timer.Task() {
            override fun run() {
                handleHold()
            }
        }
        Timer.schedule(buttonHoldTask, TOUCH_HOLD_TIME_SEC)
    }

    private fun tryUseBlock() {
        val block = gameWorld.getForeMap(mobController.player.selectedX, mobController.player.selectedY)
            .takeIf { !it.isNone() }
            ?: gameWorld.getBackMap(mobController.player.selectedX, mobController.player.selectedY)
                .takeIf { !it.isNone() }
            ?: return

        useBlockActionMap[block.params.key]?.perform(
            block = block,
            x = mobController.player.selectedX,
            y = mobController.player.selectedY,
        )
    }

    private fun tryUseMob(): Boolean {
        val mob = mobController.mobs.firstOrNull { mob ->
            mob.hitbox.contains(mobController.player.cursorX, mobController.player.cursorY) &&
                mobController.player.position.cpy().sub(mob.position).len() <= MOB_HIT_RANGE
        } ?: return false

        return useMobActionMap[mob.params.key]?.perform(mob) ?: false
    }

    private fun handleUp() {
        val player = mobController.player
        val item = player.activeItem.item
        cancelHold()

        player.startHitting(false)
        player.stopHitting()

        (item as? Item.Placeable)?.let {
            placeBlockActionMap.placeToForegroundAction(
                item = item,
                x = player.selectedX,
                y = player.selectedY,
            )
        }?.takeIfTrue()
            ?: (item as? Item.Usable)?.let {
                useItemActionMap[item.useActionKey]?.perform(item, player.selectedX, player.selectedY)
                    ?: run {
                        Gdx.app.error(TAG, "use item action ${item.useActionKey} not found")
                        false
                    }
            }?.takeIfTrue()
            ?: (item as? Item.Food)?.let {
                if (player.health < player.maxHealth) {
                    player.heal(item.heal)
                    player.decreaseCurrentItemCount()
                    true
                } else {
                    false
                }
            }?.takeIfTrue()
            ?: tryUseBlock()
    }

    override fun handle(action: MouseInputAction) {
        if (action.actionKey !is MouseInputActionKey.Right) {
            if (buttonHoldTask?.isScheduled == true) {
                cancelHold()
            }
            return
        }

        if (action.actionKey.touchUp && buttonHoldTask?.isScheduled == true) {
            handleUp()
        } else if (!action.actionKey.touchUp) {
            handleDown()
        }
    }

    companion object {
        private const val TAG = "UseItemMouseInputActionHandler"
        private const val TOUCH_HOLD_TIME_SEC = 0.5f

        private const val MOB_HIT_RANGE = 3f
    }
}
