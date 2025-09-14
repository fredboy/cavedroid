package ru.fredboy.cavedroid.domain.items.model.item

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.min
import ru.fredboy.cavedroid.domain.items.model.block.Block as DomainBlockModel

@OptIn(ExperimentalContracts::class)
sealed class Item {

    abstract val params: CommonItemParams
    abstract val sprite: Sprite

    override fun hashCode(): Int = params.key.hashCode()

    override fun equals(other: Any?): Boolean = params.key == (other as Item).params.key

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
    fun toInventoryItem(amount: Int = 1, durability: Int = 1): InventoryItem {
        val durability = if (durability == 0) {
            (this as? Durable)?.durability ?: 1
        } else {
            durability
        }

        return InventoryItem(this, amount, min(durability, (this as? Durable)?.durability ?: 1))
    }

    data class Normal(
        override val params: CommonItemParams,
        override val sprite: Sprite,
    ) : Item()

    sealed class Durable : Item() {
        abstract val durability: Int
    }

    sealed class Tool : Durable() {
        abstract val mobDamageMultiplier: Float
        abstract val blockDamageMultiplier: Float
        abstract val level: Int
    }

    sealed class Armor : Durable() {
        abstract val protection: Int
        abstract val wearableSprites: WearableSprites
    }

    data class Helmet(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val wearableSprites: WearableSprites,
        override val durability: Int,
    ) : Armor()

    data class Chestplate(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val wearableSprites: WearableSprites,
        override val durability: Int,
    ) : Armor()

    data class Leggings(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val wearableSprites: WearableSprites,
        override val durability: Int,
    ) : Armor()

    data class Boots(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val protection: Int,
        override val wearableSprites: WearableSprites,
        override val durability: Int,
    ) : Armor()

    sealed class Placeable : Item() {
        abstract val block: DomainBlockModel
        override val sprite: Sprite get() = block.sprite
    }

    data class None(
        override val params: CommonItemParams,
    ) : Item() {
        override val sprite: Sprite
            get() = throw IllegalAccessException("Trying to get sprite of None")
    }

    data class Usable(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        val useActionKey: String,
        val mobKey: String?,
    ) : Item()

    data class Block(
        override val params: CommonItemParams,
        override val block: DomainBlockModel,
    ) : Placeable()

    data class Slab(
        override val params: CommonItemParams,
        val topPartBlock: DomainBlockModel.Slab,
        val bottomPartBlock: DomainBlockModel.Slab,
    ) : Placeable() {
        override val block get() = bottomPartBlock
    }

    data class Sword(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool()

    data class Shovel(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool()

    data class Axe(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool()

    data class Pickaxe(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool()

    data class Shears(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
        override val level: Int,
        override val durability: Int,
    ) : Tool()

    data class Bow(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val durability: Int,
        val stateSprites: List<Sprite>,
    ) : Tool() {
        override val mobDamageMultiplier: Float = 1f
        override val blockDamageMultiplier: Float = 1f
        override val level: Int = 1
    }

    data class Food(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        val heal: Int,
    ) : Item()

    data class WearableSprites(
        val side: Sprite,
        val front: Sprite,
        val tint: Color?,
    )
}
