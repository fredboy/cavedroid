package ru.fredboy.cavedroid.domain.items.repository

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item

interface ItemsRepository : Disposable {

    val fallbackBlock: Block.None

    val fallbackItem: Item.None

    fun initialize()

    fun getItemByKey(key: String): Item

    fun getItemByIndex(index: Int): Item

    fun getBlockByKey(key: String): Block

    fun <T : Block> getBlocksByType(type: Class<T>): List<T>

    fun getCraftingResult(input: List<InventoryItem>): InventoryItem

    fun getAllItems(): Collection<Item>

    fun reload()
}
