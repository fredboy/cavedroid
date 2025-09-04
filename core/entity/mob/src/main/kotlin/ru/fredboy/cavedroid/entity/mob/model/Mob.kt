package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.utils.Vector2Proxy
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.drop.DropAmount
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.abstraction.MobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import kotlin.math.abs

abstract class Mob(
    var direction: Direction,
    val params: MobParams,
    val behavior: MobBehavior,
) : Disposable {

    private var resetTakeDamageTask: ResetTakeDamageTask? = null

    private var _body: Body? = null

    val width get() = params.width

    val height get() = params.height

    val maxHealth get() = params.hp

    val speed get() = params.speed

    val body: Body get() = requireNotNull(_body)

    val position: Vector2 get() = body.position

    val velocity = Vector2Proxy(
        getVelocity = { body.linearVelocity },
        setVelocity = { body.linearVelocity = it },
    )

    val controlVector = Vector2()

    val mapX get() = body.position.x.toInt()
    val upperMapY get() = (body.position.y - width / 2).toInt()
    val middleMapY get() = body.position.y.toInt()
    val lowerMapY get() = (body.position.y + height / 2).toInt()

    val hitbox: Rectangle get() = Rectangle(position.x - width / 2f, position.y - height / 2f, width, height)

    private val animationSpeed = DEFAULT_ANIMATION_SPEED * (params.animationRange / DEFAULT_ANIMATION_RANGE)

    var animDelta = animationSpeed
    var anim = 0f

    var isDead = false
        protected set

    val canJump: Boolean
        get() = footContactCounter > 0 && TimeUtils.timeSinceMillis(lastJumpMs) >= JUMP_COOLDOWN_MS

    var isFlyMode = false
        set(value) {
            body.gravityScale = if (value) {
                0f
            } else {
                1f
            }
            field = value
        }

    var health = maxHealth

    var footContactCounter = 0

    private var lastJumpMs = 0L

    private var pendingBodyTransform: Vector2? = null

    var climb = false

    var canSwim = false

    var canClimb = false

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

    var autojumpCounters = IntArray(2)

    protected val tintColor: Color
        get() = if (takingDamage) {
            DAMAGE_TINT_COLOR
        } else {
            Color.WHITE
        }

    open val physicsCategory: Short
        get() = PhysicsConstants.CATEGORY_MOB

    open fun spawn(x: Float, y: Float, factory: MobPhysicsFactory) {
        if (_body != null) {
            Gdx.app.error(
                /* tag = */ TAG,
                /* message = */
                "Attempted to spawn ${this::class.simpleName} on coordinates ($x;$y), " +
                    "when mob is already spawned and alive!",
            )
            return
        }

        _body = factory.createBody(this, x, y, physicsCategory)
    }

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
        val maxAnim = params.animationRange * (if (velocityMultiplier == 0f) 1f else velocityMultiplier)

        if (velocity.x != 0f || abs(anim) > animDelta * animMultiplier) {
            anim += animDelta * animMultiplier
        } else {
            anim = 0f
        }

        if (anim > maxAnim) {
            anim = maxAnim
            animDelta = -animationSpeed
        } else if (anim < -maxAnim) {
            anim = -maxAnim
            animDelta = animationSpeed
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
            damage(-heal)
            return
        }

        if (health >= Int.MAX_VALUE - heal) {
            health = Int.MAX_VALUE - heal
        }

        health += heal
        checkHealth()
    }

    protected open fun applyMediumResistanceToBody(mobWorldAdapter: MobWorldAdapter) {
        if (isFlyMode) {
            body.linearDamping = 2f
            body.gravityScale = 0f
            return
        }

        val climbable = mobWorldAdapter.getClimbable(hitbox.apply { height *= .75f })
        val liquid = climbable as? Block.Fluid?

        canClimb = climbable != null
        canSwim = liquid != null

        val mediumResistance = climbable?.climbSpeedFactor ?: 0f
        body.linearDamping = mediumResistance

        body.gravityScale = 1f * if (climb && canClimb) -speed else 1f
    }

    fun applyPendingTransform(vector: Vector2) {
        pendingBodyTransform = vector
    }

    fun update(mobWorldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        pendingBodyTransform?.let { transform ->
            body.setTransform(transform.add(position), 0f)
            pendingBodyTransform = null
        }

        behavior.update(this, mobWorldAdapter, playerAdapter, delta)

        applyMediumResistanceToBody(mobWorldAdapter)
        if (!controlVector.isZero) {
            body.applyForceToCenter(controlVector, true)
            velocity.x = MathUtils.clamp(velocity.x, -abs(controlVector.x), abs(controlVector.x))
            if (isFlyMode) {
                velocity.y = MathUtils.clamp(velocity.y, -abs(controlVector.y), abs(controlVector.y))
            } else {
                this@Mob.controlVector.y = 0f
            }
        } else if (!isFlyMode) {
            body.linearVelocity = body.linearVelocity.cpy().apply { x = 0f }
        }

        if (position.x > mobWorldAdapter.width) {
            body.setTransform(Vector2(position.x - mobWorldAdapter.width, position.y), 0f)
        } else if (position.x < 0) {
            body.setTransform(Vector2(position.x + mobWorldAdapter.width, position.y), 0f)
        }

        if (position.y > mobWorldAdapter.height) {
            kill()
        }
    }

    open fun getDropItems(
        itemByKey: GetItemByKeyUseCase,
    ): List<InventoryItem> {
        if (params.dropInfo.isEmpty()) {
            return emptyList()
        }

        return buildList {
            for (info in params.dropInfo) {
                when (info.amount) {
                    is DropAmount.ExactAmount -> {
                        val exactAmount = info.amount as DropAmount.ExactAmount
                        add(InventoryItem(itemByKey[info.itemKey], exactAmount.amount))
                    }

                    is DropAmount.RandomChance -> {
                        val randomChanceAmount = info.amount as DropAmount.RandomChance
                        if (MathUtils.randomBoolean(randomChanceAmount.chance)) {
                            add(InventoryItem(itemByKey[info.itemKey], randomChanceAmount.amount))
                        } else {
                            continue
                        }
                    }

                    is DropAmount.RandomRange -> {
                        val randomRangeAmount = info.amount as DropAmount.RandomRange
                        if (MathUtils.randomBoolean(randomRangeAmount.chance)) {
                            add(InventoryItem(itemByKey[info.itemKey], randomRangeAmount.range.random()))
                        } else {
                            continue
                        }
                    }
                }
            }
        }
    }

    abstract fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float)

    abstract fun changeDir()

    open fun jump() {
        if (!canJump) {
            return
        }

        body.applyLinearImpulse(
            /* impulse = */ Vector2(0f, JUMP_VELOCITY).scl(body.mass),
            /* point = */ body.worldCenter,
            /* wake = */ true,
        )

        lastJumpMs = TimeUtils.millis()
    }

    override fun dispose() {
        body.world.destroyBody(body)
        _body = null
    }

    private inner class ResetTakeDamageTask : Timer.Task() {
        override fun run() {
            takingDamage = false
        }
    }

    companion object {
        private const val TAG = "Mob"

        @JvmStatic
        protected val DEFAULT_ANIMATION_SPEED = 360f

        private const val DEFAULT_ANIMATION_RANGE = 60f
        private const val DAMAGE_TINT_TIMEOUT_S = 0.5f
        private val DAMAGE_TINT_COLOR = Color((0xff8080 shl 8) or 0xFF)

        private const val JUMP_VELOCITY = -5.05f

        private const val JUMP_COOLDOWN_MS = 500L
    }
}
