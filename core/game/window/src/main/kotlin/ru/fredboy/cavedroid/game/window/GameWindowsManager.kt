package ru.fredboy.cavedroid.game.window

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.common.utils.safeCast
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.game.window.inventory.ChestInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CraftingInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.FurnaceInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.SurvivalInventoryWindow
import javax.inject.Inject

@GameScope
class GameWindowsManager @Inject constructor(
    private val tooltipManager: TooltipManager,
    private val itemsRepository: ItemsRepository,
    private val dropAdapter: DropAdapter,
    private val playerAdapter: PlayerAdapter,
) {

    var creativeScrollAmount = 0
    var isDragging = false

    var currentWindow: AbstractInventoryWindow? = null

    val currentWindowType: GameWindowType
        get() = currentWindow?.type ?: GameWindowType.NONE

    private var lastOpenCreativeTab: CreativeInventoryTabsWindow.Tab? = null
    private var recipeBookActive = false

    fun openSurvivalInventory() {
        currentWindow = SurvivalInventoryWindow(itemsRepository).apply {
            recipeBookActive = this@GameWindowsManager.recipeBookActive
        }
    }

    fun openCreativeInventory() {
        currentWindow = CreativeInventoryWindow(itemsRepository)
    }

    fun openCreativeTabsInventory() {
        currentWindow = CreativeInventoryTabsWindow(itemsRepository).apply {
            selectedTab = lastOpenCreativeTab ?: selectedTab
        }
    }

    fun openFurnace(furnace: Furnace) {
        currentWindow = FurnaceInventoryWindow(furnace)
    }

    fun openChest(chest: Chest, otherChest: Chest? = null) {
        currentWindow = ChestInventoryWindow(chest, otherChest)
    }

    fun openCrafting() {
        currentWindow = CraftingInventoryWindow(itemsRepository).apply {
            recipeBookActive = this@GameWindowsManager.recipeBookActive
        }
    }

    fun closeWindow() {
        currentWindow?.selectedItem?.let { item ->
            dropAdapter.dropItems(playerAdapter.cursorX, playerAdapter.cursorY, listOf(item))
            currentWindow?.selectedItem = null
        }
        isDragging = false
        (currentWindow as? AbstractInventoryWindowWithCraftGrid)?.let { window ->
            recipeBookActive = window.recipeBookActive
            dropAdapter.dropItems(playerAdapter.cursorX, playerAdapter.cursorY, window.craftingItems)
        }

        currentWindow?.safeCast<CreativeInventoryTabsWindow>()?.selectedTab?.let { tab ->
            lastOpenCreativeTab = tab
        }

        currentWindow = null
        tooltipManager.showMouseTooltip("")
    }
}
