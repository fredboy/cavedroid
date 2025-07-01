package ru.fredboy.cavedroid.entity.container.model

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import kotlin.reflect.KClass

abstract class Container(
    val size: Int,
    protected val fallbackItem: Item.None,
    initialItems: List<InventoryItem>? = null,
) {

    val inventory = Inventory(size, fallbackItem, initialItems)

    val items get() = inventory.items

    abstract val type: KClass<out Block.Container>

    abstract fun update(itemByKey: GetItemByKeyUseCase)
}
