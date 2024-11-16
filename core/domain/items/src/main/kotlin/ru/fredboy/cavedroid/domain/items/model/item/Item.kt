package ru.fredboy.cavedroid.domain.items.model.item

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import ru.fredboy.cavedroid.domain.items.model.block.Block as DomainBlockModel

@OptIn(ExperimentalContracts::class)
sealed class Item {

    abstract val params: CommonItemParams
    abstract val sprite: Sprite

    override fun hashCode(): Int {
        return params.key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return params.key == (other as Item).params.key
    }

    fun isNone(): Boolean {
        contract { returns(true) implies (this@Item is None) }
        return this is None
    }

    fun isPlaceable(): Boolean {
        contract { returns(true) implies (this@Item is Placeable) }
        return this is Placeable
    }

    fun isSlab(): Boolean {
        contract { returns(true) implies (this@Item is Slab) }
        return this is Slab
    }

    fun isTool(): Boolean {
        contract { returns(true) implies (this@Item is Tool) }
        return this is Tool
    }

    fun isShears(): Boolean {
        contract { returns(true) implies (this@Item is Shears) }
        return this is Shears
    }

    fun isUsable(): Boolean {
        contract { returns(true) implies (this@Item is Usable) }
        return this is Usable
    }

    fun isFood(): Boolean {
        contract { returns(true) implies (this@Item is Food) }
        return this is Food
    }

    @JvmOverloads
    fun toInventoryItem(amount: Int = 1): InventoryItem {
        return InventoryItem(this, amount)
    }

    data class Normal(
        override val params: CommonItemParams,
        override val sprite: Sprite
    ) : Item()

    sealed class Tool : Item() {
        abstract val mobDamageMultiplier: Float
        abstract val blockDamageMultiplier: Float
        abstract val level: Int
    }

    sealed class Placeable : Item() {
        abstract val block: DomainBlockModel
        override val sprite: Sprite get() = block.sprite
    }

    data class None(
        override val params: CommonItemParams,
    ): Item() {
        override val sprite: Sprite
            get() = throw IllegalAccessException("Trying to get sprite of None")
    }

    data class Usable(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        val useActionKey: String
    ) : Item()

    data class Block(
        override val params: CommonItemParams,
        override val block: DomainBlockModel
    ) : Placeable()

    data class Slab(
        override val params: CommonItemParams,
        val topPartBlock: DomainBlockModel.Slab,
        val bottomPartBlock: DomainBlockModel.Slab
    ) : Placeable() {
        override val block get() = bottomPartBlock
    }

    data class Sword(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
    ) : Tool()

    data class Shovel(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
    ) : Tool()

    data class Axe(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
    ) : Tool()

    data class Pickaxe(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
    ) : Tool()

    data class Shears(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
    ) : Tool()

    data class Food(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        val heal: Int,
    ) : Item()

}