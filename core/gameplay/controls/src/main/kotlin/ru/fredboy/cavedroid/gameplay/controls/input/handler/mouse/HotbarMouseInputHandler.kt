package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard.DropItemKeyboardInputHandler.Companion.DROP_DISTANCE
import ru.fredboy.cavedroid.gameplay.controls.input.isInsideHotbar
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class HotbarMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val dropController: DropController,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private val hotbarTexture get() = requireNotNull(textureRegions["hotbar"])

    private var buttonHoldTask: Timer.Task? = null

    override fun checkConditions(action: MouseInputAction): Boolean = buttonHoldTask?.isScheduled == true ||
        (
            (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
                action.isInsideHotbar(gameContextRepository, textureRegions) ||
                action.actionKey is MouseInputActionKey.Scroll
            ) &&
        gameWindowsManager.currentWindowType == GameWindowType.NONE

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun createDrop(item: Item, playerX: Float, playerY: Float, amount: Int) {
        dropController.addDrop(
            x = playerX + DROP_DISTANCE * mobController.player.direction.basis,
            y = playerY,
            inventoryItem = item.toInventoryItem(amount),
            initialForce = Vector2(50f * mobController.player.direction.basis, -50f),
        )
    }

    private fun getActionSlot(action: MouseInputAction): Int = (
        (
            action.screenX -
                (gameContextRepository.getCameraContext().viewport.width / 2 - hotbarTexture.regionWidth / 2)
            ) /
            HOTBAR_CELL_WIDTH
        ).toInt()

    private fun handleHold(action: MouseInputAction) {
//        buttonHoldTask = null
//        gameWindowsManager.openInventory()
        val player = mobController.player
        val actionSlot = getActionSlot(action)
        val currentItem = player.inventory.items[actionSlot]
        val dropAmount = if (currentItem.item is Item.Tool || currentItem.item is Item.Armor) currentItem.amount else 1

        createDrop(currentItem.item, player.position.x, player.position.y, dropAmount)
        player.inventory.decreaseItemAmount(actionSlot, dropAmount)
    }

    private fun handleDown(action: MouseInputAction) {
        buttonHoldTask = object : Timer.Task() {
            override fun run() {
                handleHold(action)
            }
        }

        Timer.schedule(buttonHoldTask, TOUCH_HOLD_TIME_SEC)
    }

    private fun handleUp(action: MouseInputAction) {
        mobController.player.activeSlot = getActionSlot(action)
    }

    private fun handleScroll(action: MouseInputAction) {
        if (action.actionKey !is MouseInputActionKey.Scroll) {
            return
        }
        mobController.player.activeSlot += action.actionKey.amountY.toInt()
        if (mobController.player.activeSlot < 0) {
            mobController.player.activeSlot = Player.HOTBAR_SIZE - 1
        } else if (mobController.player.activeSlot >= Player.HOTBAR_SIZE) {
            mobController.player.activeSlot = 0
        }
    }

    override fun handle(action: MouseInputAction) {
        if (buttonHoldTask != null && buttonHoldTask?.isScheduled == true) {
            cancelHold()
        }

        if (buttonHoldTask != null && buttonHoldTask?.isScheduled != true) {
            buttonHoldTask = null
            return
        }

        if (action.actionKey !is MouseInputActionKey.Left && action.actionKey !is MouseInputActionKey.Screen) {
            if (action.actionKey is MouseInputActionKey.Scroll) {
                handleScroll(action)
            }
            return
        }

        if (action.actionKey.touchUp) {
            handleUp(action)
        } else {
            handleDown(action)
        }
    }

    companion object {
        private const val TOUCH_HOLD_TIME_SEC = 0.5f
        private const val HOTBAR_CELL_WIDTH = 20
    }
}
