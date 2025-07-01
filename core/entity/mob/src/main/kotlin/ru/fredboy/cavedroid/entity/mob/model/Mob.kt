package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.utils.bl
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.abstraction.MobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import kotlin.math.abs

abstract class Mob(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    var direction: Direction,
    val maxHealth: Int,
    val behavior: MobBehavior,
) : Rectangle(x, y, width, height) {

    private var resetTakeDamageTask: ResetTakeDamageTask? = null

    var velocity = Vector2()
        protected set

    val mapX get() = (x + width / 2).bl
    val upperMapY get() = y.bl
    val middleMapY get() = (y + height / 2).bl
    val lowerMapY get() = (y + height).bl

    var animDelta = ANIMATION_SPEED
    var anim = 0f

    var isDead = false
        protected set

    var canJump = false

    var isFlyMode = false

    var health = maxHealth

    var takingDamage = false
        set(value) {
            field = value

            if (value) {
                var resetTask = resetTakeDamageTask
                if (resetTask != null && resetTask.isScheduled) {
                    resetTask.cancel()
                } else {
                    resetTask = ResetTakeDamageTask()
                }
                Timer.schedule(resetTask, DAMAGE_TINT_TIMEOUT_S)
                resetTakeDamageTask = resetTask
            }
        }

    protected val tintColor: Color
        get() = if (takingDamage) {
            DAMAGE_TINT_COLOR
        } else {
            Color.WHITE
        }

    abstract val speed: Float

    private fun isAnimationIncreasing(): Boolean = anim > 0 && animDelta > 0 || anim < 0 && animDelta < 0

    private fun checkHealth() {
        health = MathUtils.clamp(health, 0, maxHealth)

        if (health <= 0) {
            kill()
        }
    }

    protected fun updateAnimation(delta: Float) {
        val velocityMultiplier = abs(velocity.x) / speed
        val animMultiplier = (if (velocityMultiplier == 0f) 1f else velocityMultiplier) * delta
        val maxAnim = ANIMATION_RANGE * (if (velocityMultiplier == 0f) 1f else velocityMultiplier)

        if (velocity.x != 0f || abs(anim) > animDelta * animMultiplier) {
            anim += animDelta * animMultiplier
        } else {
            anim = 0f
        }

        if (anim > maxAnim) {
            anim = maxAnim
            animDelta = -ANIMATION_SPEED
        } else if (anim < -maxAnim) {
            anim = -maxAnim
            animDelta = ANIMATION_SPEED
        }

        if (velocity.x == 0f && isAnimationIncreasing()) {
            animDelta = -animDelta
        }
    }

    protected fun switchDir() {
        direction = if (looksLeft()) {
            Direction.RIGHT
        } else {
            Direction.LEFT
        }
    }

    fun looksLeft() = direction == Direction.LEFT

    fun looksRight() = direction == Direction.RIGHT

    fun kill() {
        isDead = true
    }

    open fun damage(damage: Int) {
        if (damage == 0) {
            return
        }

        if (damage < 0) {
            Gdx.app.error(TAG, "Damage can't be negative!")
            return
        }

        if (health <= Int.MIN_VALUE + damage) {
            health = Int.MIN_VALUE + damage
        }

        health -= damage
        checkHealth()

        takingDamage = true
    }

    fun heal(heal: Int) {
        if (heal < 0) {
            Gdx.app.error(TAG, "Heal can't be negative")
            return
        }

        if (health >= Int.MAX_VALUE - heal) {
            health = Int.MAX_VALUE - heal
        }

        health += heal
        checkHealth()
    }

    fun getHitBox(): Rectangle = Rectangle(
        /* x = */ x - HIT_RANGE,
        /* y = */ y + HIT_RANGE,
        /* width = */ width + HIT_RANGE * 2f,
        /* height = */ height + HIT_RANGE * 2f,
    )

    fun update(mobWorldAdapter: MobWorldAdapter, delta: Float) {
        behavior.update(this, mobWorldAdapter, delta)
    }

    open fun getDropItems(
        itemByKey: GetItemByKeyUseCase,
    ): List<InventoryItem> = emptyList()

    abstract fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float)

    abstract fun changeDir()

    abstract fun jump()

    private inner class ResetTakeDamageTask : Timer.Task() {
        override fun run() {
            takingDamage = false
        }
    }

    companion object {
        private const val TAG = "Mob"

        @JvmStatic
        protected val ANIMATION_SPEED = 360f

        protected const val ANIMATION_RANGE = 60f

        private const val HIT_RANGE = 8f

        private const val DAMAGE_TINT_TIMEOUT_S = 0.5f
        private val DAMAGE_TINT_COLOR = Color((0xff8080 shl 8) or 0xFF)
    }
}
