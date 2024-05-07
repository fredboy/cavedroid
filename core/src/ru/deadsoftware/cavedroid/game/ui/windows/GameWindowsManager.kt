package ru.deadsoftware.cavedroid.game.ui.windows

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.DropController
import ru.deadsoftware.cavedroid.game.ui.TooltipManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.AbstractInventoryWindow
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.CraftingInventoryWindow
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.CreativeInventoryWindow
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.SurvivalInventoryWindow
import javax.inject.Inject

@GameScope
class GameWindowsManager @Inject constructor(
    private val tooltipManager: TooltipManager,
    private val mobsController: MobsController,
    private val dropController: DropController,
) {

    var creativeScrollAmount = 0
    var isDragging = false

    var currentWindow: AbstractInventoryWindow? = null

    @JvmName("getCurrentWindowType")
    fun getCurrentWindow(): GameUiWindow {
        return currentWindow?.type ?: GameUiWindow.NONE
    }

    fun openInventory() {
        if (mobsController.player.gameMode == 1) {
            currentWindow = CreativeInventoryWindow(GameUiWindow.CREATIVE_INVENTORY)
        } else {
            currentWindow = SurvivalInventoryWindow(GameUiWindow.SURVIVAL_INVENTORY)
        }
    }

    fun openCrafting() {
        currentWindow = CraftingInventoryWindow(GameUiWindow.CRAFTING_TABLE)
    }

    fun closeWindow() {
        (currentWindow as? SurvivalInventoryWindow)?.let { window ->
            window.craftingItems.forEach { item ->
                item?.item?.let {
                    dropController.addDrop(mobsController.player.x, mobsController.player.y, it, item.amount)
                }
            }
        }

        (currentWindow as? CraftingInventoryWindow)?.let { window ->
            window.craftingItems.forEach { item ->
                item?.item?.let {
                    dropController.addDrop(mobsController.player.x, mobsController.player.y, it, item.amount)
                }
            }
        }

        currentWindow = null
        tooltipManager.showMouseTooltip("")
    }

}