package ru.deadsoftware.cavedroid.game.ui.windows

import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.ui.TooltipManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.*
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.container.model.Chest
import ru.fredboy.cavedroid.game.controller.container.model.Furnace
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
class GameWindowsManager @Inject constructor(
    private val tooltipManager: TooltipManager,
    private val mobController: MobController,
    private val dropController: DropController,
    private val itemsRepository: ItemsRepository,
) {

    var creativeScrollAmount = 0
    var isDragging = false

    var currentWindow: AbstractInventoryWindow? = null

    @JvmName("getCurrentWindowType")
    fun getCurrentWindow(): GameUiWindow {
        return currentWindow?.type ?: GameUiWindow.NONE
    }

    fun openInventory() {
        if (mobController.player.gameMode == 1) {
            currentWindow = CreativeInventoryWindow()
        } else {
            currentWindow = SurvivalInventoryWindow(itemsRepository)
        }
    }

    fun openFurnace(furnace: Furnace) {
        currentWindow = FurnaceInventoryWindow(furnace)
    }

    fun openChest(chest: Chest) {
        currentWindow = ChestInventoryWindow(chest)
    }

    fun openCrafting() {
        currentWindow = CraftingInventoryWindow(itemsRepository)
    }

    fun closeWindow() {
        (currentWindow as? AbstractInventoryWindowWithCraftGrid)?.let { window ->
            window.craftingItems.forEach { item ->
                dropController.addDrop(mobController.player.x, mobController.player.y, item)
            }
        }

        currentWindow = null
        tooltipManager.showMouseTooltip("")
    }

}