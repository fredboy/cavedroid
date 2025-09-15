package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType

class SurvivalInventoryWindow(
    itemsRepository: ItemsRepository,
) : AbstractInventoryWindowWithCraftGrid(itemsRepository) {

    override val gridSize: Int
        get() = 2

    override val type = GameWindowType.SURVIVAL_INVENTORY

    override var selectedItem: InventoryItem? = null
}
