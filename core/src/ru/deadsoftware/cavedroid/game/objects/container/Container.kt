package ru.deadsoftware.cavedroid.game.objects.container

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import java.io.Serializable
import javax.annotation.OverridingMethodsMustInvokeSuper

abstract class Container(
    val size: Int,
    gameItemsHolder: GameItemsHolder
) : Serializable {

    private val _items = Array(size) { gameItemsHolder.fallbackItem.toInventoryItem() }

    val items get() = _items.asList() as MutableList<InventoryItem>

    @OverridingMethodsMustInvokeSuper
    open fun initItems(gameItemsHolder: GameItemsHolder) {
        _items.forEach { it.init(gameItemsHolder) }
    }

    abstract fun update(gameItemsHolder: GameItemsHolder)

}