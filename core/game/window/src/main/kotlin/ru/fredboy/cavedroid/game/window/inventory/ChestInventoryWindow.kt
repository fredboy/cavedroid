package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.game.window.GameWindowType

class ChestInventoryWindow(val chest: Chest) : AbstractInventoryWindow() {

    override val type = GameWindowType.CHEST

    override var selectedItem: InventoryItem? = null

}