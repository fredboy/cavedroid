package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs

class CreativeInventoryWindow() : AbstractInventoryWindow() {

    override val type = GameWindowType.CREATIVE_INVENTORY

    override var selectedItem: InventoryItem? = null

    fun getMaxScroll(itemsRepository: ItemsRepository): Int {
        return itemsRepository.getAllItems().size / GameWindowsConfigs.Creative.itemsInRow
    }
}
