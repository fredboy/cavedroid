package ru.fredboy.cavedroid.ux.controls.input.handler.mouse

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.controls.action.placeToBackgroundAction
import ru.fredboy.cavedroid.ux.controls.action.placeToForegroundAction
import ru.fredboy.cavedroid.ux.controls.action.placeblock.IPlaceBlockAction
import ru.fredboy.cavedroid.ux.controls.action.useblock.IUseBlockAction
import ru.fredboy.cavedroid.ux.controls.action.useitem.IUseItemAction
import ru.fredboy.cavedroid.ux.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.ux.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.ux.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.ux.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.ux.controls.input.isInsideHotbar
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class UseItemMouseInputHandler @Inject constructor(
    private val mobController: MobController,
    private val useItemActionMap: Map<String, @JvmSuppressWildcards IUseItemAction>,
    private val placeBlockActionMap: Map<String, @JvmSuppressWildcards IPlaceBlockAction>,
    private val useBlockActionMap: Map<String, @JvmSuppressWildcards IUseBlockAction>,
    private val gameWindowsManager: GameWindowsManager,
    private val gameWorld: GameWorld,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private var buttonHoldTask: Timer.Task? = null

    override fun checkConditions(action: MouseInputAction): Boolean {
        return buttonHoldTask?.isScheduled == true ||
            !action.isInsideHotbar(textureRegions) &&
            gameWindowsManager.currentWindowType == GameWindowType.NONE &&
            action.actionKey is MouseInputActionKey.Right
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
                x = player.cursorX,
                y = player.cursorY,
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
        val block = gameWorld.getForeMap(mobController.player.cursorX, mobController.player.cursorY)
            .takeIf { !it.isNone() }
            ?: gameWorld.getBackMap(mobController.player.cursorX, mobController.player.cursorY)
                .takeIf { !it.isNone() }
            ?: return

        useBlockActionMap[block.params.key]?.perform(
            block = block,
            x = mobController.player.cursorX,
            y = mobController.player.cursorY,
        )
    }

    private fun handleUp() {
        val player = mobController.player
        val item = player.activeItem.item
        cancelHold()

        player.startHitting(false)
        player.stopHitting()

        if (item is Item.Placeable) {
            placeBlockActionMap.placeToForegroundAction(
                item = item,
                x = player.cursorX,
                y = player.cursorY,
            )
        } else if (item is Item.Usable) {
            useItemActionMap[item.useActionKey]?.perform(item, player.cursorX, player.cursorY)
                ?: Gdx.app.error(TAG, "use item action ${item.useActionKey} not found")
        } else if (item is Item.Food && player.health < player.maxHealth) {
            player.heal(item.heal)
            player.decreaseCurrentItemCount()
        } else {
            tryUseBlock()
        }
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
    }
}
