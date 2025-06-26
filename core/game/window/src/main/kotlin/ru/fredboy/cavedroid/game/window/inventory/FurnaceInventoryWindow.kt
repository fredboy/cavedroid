package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.game.window.GameWindowType

class FurnaceInventoryWindow(
    val furnace: Furnace,
) : AbstractInventoryWindow() {

    override val type = GameWindowType.FURNACE

    override var selectedItem: InventoryItem? = null

}