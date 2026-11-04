package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
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

    private var pressedKey: MouseInputActionKey? = null
    private var holdTriggered = false

    private fun ownsPress(key: MouseInputActionKey): Boolean = when (val pressed = pressedKey) {
        is MouseInputActionKey.Screen -> key is MouseInputActionKey.Touch && key.pointer == pressed.pointer
        is MouseInputActionKey.Left ->
            key is MouseInputActionKey.Left ||
                (key is MouseInputActionKey.Dragged && key.pointer == 0)
        else -> false
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        if (gameWindowsManager.currentWindowType != GameWindowType.NONE) return false
        if (pressedKey != null) return ownsPress(action.actionKey)

        val key = action.actionKey
        return when (key) {
            is MouseInputActionKey.Scroll -> true
            is MouseInputActionKey.Left -> !key.touchUp && action.isInsideHotbar(gameContextRepository, textureRegions)
            is MouseInputActionKey.Screen ->
                !key.touchUp &&
                    (!gameContextRepository.getJoystick().active || key.pointer != gameContextRepository.getJoystick().pointer) &&
                    action.isInsideHotbar(gameContextRepository, textureRegions)
            else -> false
        }
    }

    private fun cancelHold() {
        buttonHoldTask?.cancel()
        buttonHoldTask = null
    }

    private fun createDrop(item: InventoryItem, dropAmount: Int) {
        val player = mobController.player
        dropController.addDrop(
            x = player.cursorX,
            y = player.cursorY,
            inventoryItem = item.copy().apply { amount = dropAmount },
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
        val player = mobController.player
        val actionSlot = getActionSlot(action)
        val currentItem = player.inventory.items[actionSlot]
        val dropAmount = if (currentItem.item is Item.Tool || currentItem.item is Item.Armor) currentItem.amount else 1

        createDrop(currentItem, dropAmount)
        player.inventory.decreaseItemAmount(actionSlot, dropAmount)
    }

    private fun handleDown(action: MouseInputAction) {
        pressedKey = action.actionKey
        holdTriggered = false
        buttonHoldTask = object : Timer.Task() {
            override fun run() {
                holdTriggered = true
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
        if (pressedKey != null && !ownsPress(action.actionKey)) return

        if (action.actionKey is MouseInputActionKey.Scroll) {
            handleScroll(action)
            return
        }

        if (pressedKey == null) {
            if ((action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
                !action.actionKey.touchUp
            ) {
                handleDown(action)
            }
            return
        }

        val selectSlot = action.actionKey !is MouseInputActionKey.Dragged &&
            action.actionKey.touchUp && !holdTriggered &&
            action.isInsideHotbar(gameContextRepository, textureRegions)
        reset()
        if (selectSlot) handleUp(action)
    }

    override fun reset() {
        cancelHold()
        pressedKey = null
        holdTriggered = false
    }

    companion object {
        private const val TOUCH_HOLD_TIME_SEC = 0.5f
        private const val HOTBAR_CELL_WIDTH = 20
    }
}
