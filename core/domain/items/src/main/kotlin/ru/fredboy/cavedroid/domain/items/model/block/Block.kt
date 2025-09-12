package ru.fredboy.cavedroid.domain.items.model.block

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.utils.PIXELS_PER_METER
import ru.fredboy.cavedroid.common.utils.colorFromHexString
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.items.model.drop.DropAmount
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
sealed class Block {

    abstract val params: CommonBlockParams

    val width get() = 1f - params.collisionMargins.left - params.collisionMargins.right
    val height get() = 1f - params.collisionMargins.top - params.collisionMargins.bottom

    val spriteWidthMeters get() = (PIXELS_PER_METER - params.spriteMargins.left - params.spriteMargins.right).meters
    val spriteHeightMeters get() = (PIXELS_PER_METER - params.spriteMargins.top - params.spriteMargins.bottom).meters

    protected var animation: Array<Sprite>? = null

    private var _sprite: Sprite? = null
        get() {
            return animation?.get(currentAnimationFrame) ?: field
        }

    open val sprite: Sprite
        get() = requireNotNull(_sprite) { "null sprite for block '${params.key}'" }

    private val currentAnimationFrame: Int
        get() {
            return params.animationInfo?.let { animInfo ->
                ((TimeUtils.millis() / ANIMATION_FRAME_DURATION_MS) % animInfo.framesCount).toInt()
            } ?: 0
        }

    override fun hashCode(): Int = params.key.hashCode()

    override fun equals(other: Any?): Boolean = params.key == (other as Item).params.key

    fun initialize() {
        initAnimation()
        initSprite()
    }

    private fun initAnimation() {
        animation = params.animationInfo?.let { animInfo ->
            requireNotNull(params.texture) { "Cannot derive animation frames from null sprite" }
            Array(animInfo.framesCount) { y ->
                val width = 16 - params.spriteMargins.left - params.spriteMargins.right
                val height = 16 - params.spriteMargins.top - params.spriteMargins.bottom
                Sprite(params.texture, params.spriteMargins.left, 16 * y + params.spriteMargins.top, width, height)
                    .apply {
                        flip(false, true)
                        params.tint?.let { tint -> color = colorFromHexString(tint) }
                    }
            }
        }
    }

    private fun initSprite() {
        _sprite = animation?.get(0) ?: params.texture?.let { tex ->
            val width = 16 - params.spriteMargins.left - params.spriteMargins.right
            val height = 16 - params.spriteMargins.top - params.spriteMargins.bottom
            Sprite(tex, params.spriteMargins.left, params.spriteMargins.top, width, height)
                .apply {
                    flip(false, true)
                    params.tint?.let { tint -> color = colorFromHexString(tint) }
                }
        }
    }

    fun requireSprite() = requireNotNull(sprite)

    fun draw(spriter: SpriteBatch, x: Float, y: Float) {
        sprite.apply {
            setBounds(
                /* x = */ x + params.spriteMarginsMeters.left,
                /* y = */ y + params.spriteMarginsMeters.top,
                /* width = */ spriteWidthMeters,
                /* height = */ spriteHeightMeters,
            )
            draw(spriter)
        }
    }

    fun isFluid(): Boolean {
        contract { returns(true) implies (this@Block is Fluid) }
        return this is Fluid
    }

    fun isWater(): Boolean {
        contract { returns(true) implies (this@Block is Water) }
        return this is Water
    }

    fun isLava(): Boolean {
        contract { returns(true) implies (this@Block is Lava) }
        return this is Lava
    }

    fun isSlab(): Boolean {
        contract { returns(true) implies (this@Block is Slab) }
        return this is Slab
    }

    fun isContainer(): Boolean {
        contract { returns(true) implies (this@Block is Container) }
        return this is Container
    }

    fun isFurnace(): Boolean {
        contract { returns(true) implies (this@Block is Furnace) }
        return this is Furnace
    }

    fun isChest(): Boolean {
        contract { returns(true) implies (this@Block is Chest) }
        return this is Chest
    }

    fun isNone(): Boolean {
        contract { returns(true) implies (this@Block is None) }
        return this is None
    }

    fun getRectangle(x: Int, y: Int): Rectangle = Rectangle(
        /* x = */ x.toFloat() + params.collisionMargins.left,
        /* y = */ y.toFloat() + params.collisionMargins.top,
        /* width = */ width,
        /* height = */ height,
    )

    fun getSpriteRectangle(x: Int, y: Int): Rectangle = Rectangle(
        /* x = */ x.toFloat() + params.spriteMarginsMeters.left,
        /* y = */ y.toFloat() + params.spriteMarginsMeters.top,
        /* width = */ spriteWidthMeters,
        /* height = */ spriteHeightMeters,
    )

    fun getDropItem(itemByKey: GetItemByKeyUseCase, toolRequirementMet: Boolean): InventoryItem? {
        if (params.dropInfo.isEmpty()) {
            return null
        }

        for (info in params.dropInfo) {
            if (info.requiresTool && !toolRequirementMet) {
                continue
            }

            when (info.amount) {
                is DropAmount.ExactAmount -> {
                    return InventoryItem(itemByKey[info.itemKey], info.amount.amount)
                }

                is DropAmount.RandomChance -> {
                    if (MathUtils.randomBoolean(info.amount.chance)) {
                        return InventoryItem(itemByKey[info.itemKey], info.amount.amount)
                    } else {
                        continue
                    }
                }

                is DropAmount.RandomRange -> {
                    if (MathUtils.randomBoolean(info.amount.chance)) {
                        return InventoryItem(itemByKey[info.itemKey], info.amount.range.random())
                    } else {
                        continue
                    }
                }
            }
        }

        return null
    }

    sealed class Container : Block()

    data class None(
        override val params: CommonBlockParams,
    ) : Block()

    data class Normal(
        override val params: CommonBlockParams,
    ) : Block()

    data class Furnace(
        override val params: CommonBlockParams,
    ) : Container() {

        override val sprite: Sprite
            get() = getSprite(false)

        private fun getSprite(isActive: Boolean): Sprite = animation?.let { animation ->
            if (isActive) {
                animation[1]
            } else {
                animation[0]
            }
        } ?: sprite

        fun draw(spriter: SpriteBatch, x: Float, y: Float, isActive: Boolean) {
            getSprite(isActive).apply {
                setBounds(
                    /* x = */ x + params.spriteMarginsMeters.left,
                    /* y = */ y + params.spriteMarginsMeters.top,
                    /* width = */ spriteWidthMeters,
                    /* height = */ spriteHeightMeters,
                )
                draw(spriter)
            }
        }
    }

    data class Chest(
        override val params: CommonBlockParams,
    ) : Container()

    data class Slab(
        override val params: CommonBlockParams,
        val fullBlockKey: String,
        val otherPartBlockKey: String,
    ) : Block()

    sealed class Fluid : Climbable() {
        abstract val state: Int
        abstract val density: Float

        override val climbSpeedFactor: Float
            get() = density
    }

    data class Water(
        override val params: CommonBlockParams,
        override val state: Int,
        override val density: Float,
    ) : Fluid()

    data class Lava(
        override val params: CommonBlockParams,
        override val state: Int,
        override val density: Float,
    ) : Fluid()

    sealed class Climbable : Block() {
        abstract val climbSpeedFactor: Float
    }

    data class Ladder(
        override val params: CommonBlockParams,
        override val climbSpeedFactor: Float,
    ) : Climbable()

    data class Web(
        override val params: CommonBlockParams,
        override val climbSpeedFactor: Float,
    ) : Climbable()

    companion object {
        private const val ANIMATION_FRAME_DURATION_MS = 100L
    }
}
