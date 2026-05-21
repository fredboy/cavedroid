package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs

class CreativeInventoryWindow(
    private val itemsRepository: ItemsRepository,
) : AbstractInventoryWindowWithScroll() {

    override val type = GameWindowType.CREATIVE_INVENTORY

    override var selectedItem: InventoryItem? = null

    override fun getMaxScroll(): Int {
        return itemsRepository.getAllItems().size / GameWindowsConfigs.Creative.itemsInRow
    }
}
