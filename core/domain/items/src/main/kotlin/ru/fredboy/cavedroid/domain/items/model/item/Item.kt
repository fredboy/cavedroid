package ru.fredboy.cavedroid.domain.items.model.item

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.min
import ru.fredboy.cavedroid.domain.items.model.block.Block as DomainBlockModel

@OptIn(ExperimentalContracts::class)
fun Item.isNone(): Boolean {
    contract { returns(true) implies (this@isNone is Item.None) }
    return this is Item.None
}

@OptIn(ExperimentalContracts::class)
fun Item.isPlaceable(): Boolean {
    contract { returns(true) implies (this@isPlaceable is Item.Placeable) }
    return this is Item.Placeable
}

@OptIn(ExperimentalContracts::class)
fun Item.isSlab(): Boolean {
    contract { returns(true) implies (this@isSlab is Item.Slab) }
    return this is Item.Slab
}

@OptIn(ExperimentalContracts::class)
fun Item.isTool(): Boolean {
    contract { returns(true) implies (this@isTool is Item.Tool) }
    return this is Item.Tool
}

@OptIn(ExperimentalContracts::class)
fun Item.isShears(): Boolean {
    contract { returns(true) implies (this@isShears is Item.Shears) }
    return this is Item.Shears
}

@OptIn(ExperimentalContracts::class)
fun Item.isUsable(): Boolean {
    contract { returns(true) implies (this@isUsable is Item.Usable) }
    return this is Item.Usable
}

@OptIn(ExperimentalContracts::class)
fun Item.isFood(): Boolean {
    contract { returns(true) implies (this@isFood is Item.Food) }
    return this is Item.Food
}

sealed interface Item {

    val params: CommonItemParams
    val sprite: Sprite

    fun toInventoryItem(amount: Int = 1, durability: Int = 1): InventoryItem {
        val effectiveDurability = if (durability == 0) {
            (this as? Durable)?.durability ?: 1
        } else {
            durability
        }

        return InventoryItem(this, amount, min(effectiveDurability, (this as? Durable)?.durability ?: 1))
    }

    sealed interface Durable : Item {
        val durability: Int
    }

    sealed interface Tool : Durable {
        val mobDamageMultiplier: Float
        val blockDamageMultiplier: Float
        val level: Int
    }

    sealed interface Armor : Durable {
        val protection: Int
        val material: String
    }

    sealed interface Usable : Item {
        val useActionKey: String
        val mobKey: String?
    }

    sealed interface Placeable : Item {
        val block: DomainBlockModel
        override val sprite: Sprite get() = block.sprite
    }

    data class Normal(
        override val params: CommonItemParams,
        override val sprite: Sprite,
    ) : Item

    data class None(
        override val params: CommonItemParams,
    ) : Item {
        override val sprite: Sprite
            get() = throw IllegalAccessException("Trying to get sprite of None")
    }

    data class GenericUsable(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val useActionKey: String,
        override val mobKey: String?,
    ) : Usable

    data class FlintAndSteel(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val durability: Int,
    ) : Durable,
        Usable {
        override val useActionKey: String get() = USE_ACTION_KEY
        override val mobKey: String? get() = null

        companion object {
            const val USE_ACTION_KEY = "use_flint_and_steel"
        }
    }

    data class Helmet(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val durability: Int,
        override val material: String,
    ) : Armor

    data class Chestplate(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val durability: Int,
        override val material: String,
    ) : Armor

    data class Leggings(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val durability: Int,
        override val material: String,
    ) : Armor

    data class Boots(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val durability: Int,
        override val material: String,
    ) : Armor

    data class Block(
        override val params: CommonItemParams,
        override val block: DomainBlockModel,
    ) : Placeable

    data class Slab(
        override val params: CommonItemParams,
        val topPartBlock: DomainBlockModel.Slab,
        val bottomPartBlock: DomainBlockModel.Slab,
    ) : Placeable {
        override val block get() = bottomPartBlock
    }

    data class Sword(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool

    data class Shovel(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool

    data class Axe(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool

    data class Pickaxe(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool

    data class Shears(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool

    data class Hoe(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool

    data class Bow(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val durability: Int,
        val stateSprites: List<Sprite>,
    ) : Tool {
        override val mobDamageMultiplier: Float = 1f
        override val blockDamageMultiplier: Float = 1f
        override val level: Int = 1
    }

    data class Food(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        val heal: Int,
        val saturation: Float,
    ) : Item
}
