package ru.fredboy.cavedroid.game.window

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.game.window.inventory.ChestInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CraftingInventoryWindow
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

    fun openSurvivalInventory() {
        currentWindow = SurvivalInventoryWindow(itemsRepository)
    }

    fun openCreativeInventory() {
        currentWindow = CreativeInventoryWindow()
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
            dropAdapter.dropItems(playerAdapter.x, playerAdapter.y, window.craftingItems)
        }
        currentWindow = null
        tooltipManager.showMouseTooltip("")
    }
}
