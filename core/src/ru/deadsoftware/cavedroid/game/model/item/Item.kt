package ru.deadsoftware.cavedroid.game.model.item

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.deadsoftware.cavedroid.game.model.block.Block
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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

    fun isPlaceable(): Boolean {
        contract { returns(true) implies (this@Item is Placeable) }
        return this is Placeable
    }

    fun isTool(): Boolean {
        contract { returns(true) implies (this@Item is Tool) }
        return this is Tool
    }

    fun isUsable(): Boolean {
        contract { returns(true) implies (this@Item is Placeable) }
        return this is Placeable
    }
    
    sealed class Tool : Item() {
        abstract val mobDamageMultiplier: Float
        abstract val blockDamageMultiplier: Float
    }

    sealed class Usable : Item() {
        abstract val useActionKey: String
    }
    
    data class Placeable(
        override val params: CommonItemParams,
        val block: Block
    ) : Item() {
        override val sprite: Sprite get() = block.sprite
    }
    
    data class Sword(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
    ) : Tool()

    data class Shovel(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val mobDamageMultiplier: Float,
        override val blockDamageMultiplier: Float,
    ) : Tool()
    
    data class Bucket(
        override val params: CommonItemParams,
        override val sprite: Sprite,
        override val useActionKey: String
    ) : Usable()

}