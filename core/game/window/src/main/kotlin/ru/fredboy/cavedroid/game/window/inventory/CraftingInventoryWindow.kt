package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType

class CraftingInventoryWindow(
    itemsRepository: ItemsRepository,
) : AbstractInventoryWindowWithCraftGrid(itemsRepository) {

    override val type = GameWindowType.CRAFTING_TABLE

    override var selectedItem: InventoryItem? = null
}
