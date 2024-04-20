package ru.deadsoftware.cavedroid.game.model.block

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.TimeUtils
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
sealed class Block {

    abstract val params: CommonBlockParams

    val width: Float get() = 16f - params.collisionMargins.left - params.collisionMargins.right
    val height: Float get() = 16f - params.collisionMargins.top - params.collisionMargins.bottom

    private val spriteWidth: Float get() = 16f - params.spriteMargins.left - params.spriteMargins.right
    private val spriteHeight: Float get() = 16f - params.spriteMargins.top - params.spriteMargins.bottom

    private var animation: Array<Sprite>? = null

    private var _sprite: Sprite? = null
        get() {
            return animation?.get(currentAnimationFrame) ?: field
        }

    val sprite: Sprite
        get() = requireNotNull(_sprite)

    private val currentAnimationFrame: Int
        get() {
            return params.animationInfo?.let { animInfo ->
                ((TimeUtils.millis() / ANIMATION_FRAME_DURATION_MS) % animInfo.framesCount).toInt()
            } ?: 0
        }

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
                    .apply { flip(false, true) }
            }
        }
    }

    private fun initSprite() {
        _sprite = animation?.get(0) ?: params.texture?.let { tex ->
            val width = 16 - params.spriteMargins.left - params.spriteMargins.right
            val height = 16 - params.spriteMargins.top - params.spriteMargins.bottom
            Sprite(tex, params.spriteMargins.left, params.spriteMargins.top, width, height)
                .apply { flip(false, true) }
        }
    }

    fun requireSprite() = requireNotNull(sprite)

    fun draw(spriter: SpriteBatch, x: Float, y: Float) {
        sprite.apply {
            setBounds(
                /* x = */ x + params.spriteMargins.left,
                /* y = */ y + params.spriteMargins.top,
                /* width = */ spriteWidth,
                /* height = */ spriteHeight
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

    fun getRectangle(x: Int, y: Int): Rectangle {
        return Rectangle(
            /* x = */ x * 16f + params.collisionMargins.left,
            /* y = */ y * 16f + params.collisionMargins.top,
            /* width = */ width,
            /* height = */ height
        )
    }



    data class Normal(
        override val params: CommonBlockParams,
    ) : Block()

    data class Slab(
        override val params: CommonBlockParams,
        val fullBlockKey: String,
    ): Block()

    sealed class Fluid: Block()
    
    data class Water(
        override val params: CommonBlockParams,
    ) : Fluid()

    data class Lava(
        override val params: CommonBlockParams,
    ) : Fluid()

    /* Legacy accessors below */

    // collision margins
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val left: Int get() = params.collisionMargins.left
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val right: Int get() = params.collisionMargins.left
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val top: Int get() = params.collisionMargins.left
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val bottom: Int get() = params.collisionMargins.left
    
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val hp: Int get() = params.hitPoints
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val collision: Boolean get() = params.hasCollision
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val animated: Boolean get() = params.animationInfo != null
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val frames: Int get() = params.animationInfo?.framesCount ?: 0
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) val drop: String get() = params.dropInfo?.itemKey ?: "none"
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) fun hasDrop() = params.dropInfo != null
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) fun toJump() = params.hasCollision && params.collisionMargins.top < 8
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) fun hasCollision() = params.hasCollision
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) fun isBackground() = params.isBackground
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) fun isTransparent() = params.isTransparent
    @Deprecated(LEGACY_ACCESSOR_DEPRECATION) fun getTexture() = sprite


    companion object {
        private const val LEGACY_ACCESSOR_DEPRECATION = "legacy accessors will be removed"
        private const val ANIMATION_FRAME_DURATION_MS = 100L
    }
}