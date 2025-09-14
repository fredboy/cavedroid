package ru.fredboy.cavedroid.entity.mob.model

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import kotlin.reflect.KClass

class WearingArmor(
    val fallbackItem: Item.None,
) {
    val items = MutableList(4) { fallbackItem.toInventoryItem() }

    var helmet: InventoryItem
        get() = items[HELMET_INDEX]
        set(value) {
            if (value.amount > 0 && value.item !is Item.None && value.item !is Item.Helmet) {
                return
            }

            items[HELMET_INDEX] = value
        }

    var chestplate: InventoryItem
        get() = items[CHESTPLATE_INDEX]
        set(value) {
            if (value.amount > 0 && value.item !is Item.None && value.item !is Item.Chestplate) {
                return
            }

            items[CHESTPLATE_INDEX] = value
        }

    var leggings: InventoryItem
        get() = items[LEGGINGS_INDEX]
        set(value) {
            if (value.amount > 0 && value.item !is Item.None && value.item !is Item.Leggings) {
                return
            }

            items[LEGGINGS_INDEX] = value
        }

    var boots: InventoryItem
        get() = items[BOOTS_INDEX]
        set(value) {
            if (value.amount > 0 && value.item !is Item.None && value.item !is Item.Boots) {
                return
            }

            items[BOOTS_INDEX] = value
        }

    fun getTotalProtection(): Int {
        return items.sumOf { (it.item as? Item.Armor)?.protection ?: 0 }
    }

    fun damageArmorPieces() {
        for (i in items.indices) {
            items[i].item as? Item.Armor ?: continue

            items[i].durate()

            if (items[i].amount <= 0) {
                items[i] = fallbackItem.toInventoryItem()
            }
        }
    }

    fun getCellType(index: Int): KClass<out Item.Armor> {
        return when (index) {
            HELMET_INDEX -> Item.Helmet::class
            CHESTPLATE_INDEX -> Item.Chestplate::class
            LEGGINGS_INDEX -> Item.Leggings::class
            BOOTS_INDEX -> Item.Boots::class
            else -> throw IllegalArgumentException()
        }
    }

    fun clear() {
        for (i in items.indices) {
            items[i] = fallbackItem.toInventoryItem()
        }
    }

    companion object {
        private const val HELMET_INDEX = 0
        private const val CHESTPLATE_INDEX = 1
        private const val LEGGINGS_INDEX = 2
        private const val BOOTS_INDEX = 3
    }
}
