package ru.deadsoftware.cavedroid.game.model.item

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import java.io.Serializable

class InventoryItem @JvmOverloads constructor(
    val itemKey: String,
    val amount: Int = 1,
) : Serializable {

    @Transient
    lateinit var item: Item
        private set

    @JvmOverloads
    constructor(_item: Item, amount: Int = 1) : this(_item.params.key, amount) {
        item = _item
    }

    fun init(gameItemsHolder: GameItemsHolder) {
        if (this::item.isInitialized) {
            return
        }
        item = gameItemsHolder.getItem(itemKey)
    }

}
