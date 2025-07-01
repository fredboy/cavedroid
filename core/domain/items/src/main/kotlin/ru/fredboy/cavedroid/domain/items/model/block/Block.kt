package ru.fredboy.cavedroid.domain.items.model.block

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.utils.BLOCK_SIZE_PX
import ru.fredboy.cavedroid.common.utils.colorFromHexString
import ru.fredboy.cavedroid.domain.items.model.item.Item
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
sealed class Block {

    abstract val params: CommonBlockParams

    val width: Float get() = BLOCK_SIZE_PX - params.collisionMargins.left - params.collisionMargins.right
    val height: Float get() = BLOCK_SIZE_PX - params.collisionMargins.top - params.collisionMargins.bottom

    val spriteWidth: Float get() = BLOCK_SIZE_PX - params.spriteMargins.left - params.spriteMargins.right
    val spriteHeight: Float get() = BLOCK_SIZE_PX - params.spriteMargins.top - params.spriteMargins.bottom

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
                /* x = */ x + params.spriteMargins.left,
                /* y = */ y + params.spriteMargins.top,
                /* width = */ spriteWidth,
                /* height = */ spriteHeight,
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
        /* x = */ x * BLOCK_SIZE_PX + params.collisionMargins.left,
        /* y = */ y * BLOCK_SIZE_PX + params.collisionMargins.top,
        /* width = */ width,
        /* height = */ height,
    )

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
                    /* x = */ x + params.spriteMargins.left,
                    /* y = */ y + params.spriteMargins.top,
                    /* width = */ spriteWidth,
                    /* height = */ spriteHeight,
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

    sealed class Fluid : Block() {
        abstract val state: Int
    }

    data class Water(
        override val params: CommonBlockParams,
        override val state: Int,
    ) : Fluid()

    data class Lava(
        override val params: CommonBlockParams,
        override val state: Int,
    ) : Fluid()

    companion object {
        private const val ANIMATION_FRAME_DURATION_MS = 100L
    }
}
