package ru.deadsoftware.cavedroid.game.windows

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.windows.inventory.AbstractInventoryWindow
import ru.deadsoftware.cavedroid.game.windows.inventory.CraftingInventoryWindow
import ru.deadsoftware.cavedroid.game.windows.inventory.CreativeInventoryWindow
import ru.deadsoftware.cavedroid.game.windows.inventory.SurvivalInventoryWindow
import javax.inject.Inject

@GameScope
class GameWindowsManager @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
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
        currentWindow = null
    }

}