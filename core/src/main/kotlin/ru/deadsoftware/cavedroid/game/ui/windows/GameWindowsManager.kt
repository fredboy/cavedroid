package ru.deadsoftware.cavedroid.game.ui.windows

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.container.Chest
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.game.objects.container.Furnace
import ru.deadsoftware.cavedroid.game.ui.TooltipManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.*
import javax.inject.Inject

@GameScope
class GameWindowsManager @Inject constructor(
    private val tooltipManager: TooltipManager,
    private val mobsController: MobsController,
    private val dropController: DropController,
    private val gameItemsHolder: GameItemsHolder,
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
            currentWindow = CreativeInventoryWindow()
        } else {
            currentWindow = SurvivalInventoryWindow(gameItemsHolder)
        }
    }

    fun openFurnace(furnace: Furnace) {
        currentWindow = FurnaceInventoryWindow(furnace)
    }

    fun openChest(chest: Chest) {
        currentWindow = ChestInventoryWindow(chest)
    }

    fun openCrafting() {
        currentWindow = CraftingInventoryWindow(gameItemsHolder)
    }

    fun closeWindow() {
        (currentWindow as? AbstractInventoryWindowWithCraftGrid)?.let { window ->
            window.craftingItems.forEach { item ->
                dropController.addDrop(mobsController.player.x, mobsController.player.y, item)
            }
        }

        currentWindow = null
        tooltipManager.showMouseTooltip("")
    }

}