package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.utils.Vector2Proxy
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.abstraction.MobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import kotlin.math.abs

abstract class Mob(
    val width: Float,
    val height: Float,
    var direction: Direction,
    val maxHealth: Int,
    val behavior: MobBehavior,
) : Disposable {

    private var resetTakeDamageTask: ResetTakeDamageTask? = null

    private var _body: Body? = null

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

    var animDelta = ANIMATION_SPEED
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

    protected open val physicsCategory: Short
        get() = PhysicsConstants.CATEGORY_MOB

    abstract val speed: Float

    fun spawn(x: Float, y: Float, world: World) {
        if (_body != null) {
            Gdx.app.error(
                /* tag = */ TAG,
                /* message = */
                "Attempted to spawn ${this::class.simpleName} on coordinates ($x;$y), " +
                    "when mob is already spawned and alive!",
            )
            return
        }

        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(x, y)
            fixedRotation = true
        }

        _body = world.createBody(bodyDef)

        body.userData = this

        val legShapeRadius = width / 4f

        val bodyShape = PolygonShape().apply {
            setAsBox(width / 2f, height / 2f - legShapeRadius)
        }

        val bodyFixtureDef = FixtureDef().apply {
            shape = bodyShape
            density = 1f
            friction = .2f
            restitution = 0f
            filter.categoryBits = physicsCategory
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }

        val leftLegShape = CircleShape().apply {
            radius = legShapeRadius
            position = Vector2(-width / 2 + legShapeRadius, height / 2f - legShapeRadius)
        }

        val rightLegShape = CircleShape().apply {
            radius = legShapeRadius
            position = Vector2(width / 2 - legShapeRadius, height / 2f - legShapeRadius)
        }

        val leftLegFixtureDef = FixtureDef().apply {
            shape = leftLegShape
            friction = .2f
            restitution = 0f
            filter.categoryBits = physicsCategory
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }

        val rightLegFixtureDef = FixtureDef().apply {
            shape = rightLegShape
            friction = .2f
            restitution = 0f
            filter.categoryBits = physicsCategory
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }

        val jumpSensorShape = PolygonShape().apply {
            setAsBox(width / 4f, .0625f, Vector2(0f, height / 2f + .0625f), 0f)
        }

        val jumpSensorFixtureDef = FixtureDef().apply {
            shape = jumpSensorShape
            isSensor = true
            filter.categoryBits = physicsCategory
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }

        val autoJumpSensorShape = EdgeShape().apply {
            set(-2f, height / 2f - .8f, 2f, height / 2f - .8f)
        }

        val autoJumpFixtureDef = FixtureDef().apply {
            shape = autoJumpSensorShape
            isSensor = true
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }

        body.createFixture(bodyFixtureDef)
        body.createFixture(leftLegFixtureDef)
        body.createFixture(rightLegFixtureDef)
        body.createFixture(jumpSensorFixtureDef).apply {
            userData = ContactSensorType.MOB_ON_GROUND
        }
        body.createFixture(autoJumpFixtureDef).apply {
            userData = ContactSensorType.MOB_SHOULD_JUMP
        }

        body.linearDamping = 1f

        bodyShape.dispose()
        leftLegShape.dispose()
        rightLegShape.dispose()
        jumpSensorShape.dispose()
        autoJumpSensorShape.dispose()
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

    fun update(mobWorldAdapter: MobWorldAdapter, delta: Float) {
        behavior.update(this, mobWorldAdapter, delta)

        if (!controlVector.isZero) {
            body.applyForceToCenter(controlVector, true)
            velocity.x = MathUtils.clamp(velocity.x, -abs(controlVector.x), abs(controlVector.x))
            if (isFlyMode) {
                velocity.y = MathUtils.clamp(velocity.y, -abs(controlVector.y), abs(controlVector.y))
            }
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
    ): List<InventoryItem> = emptyList()

    abstract fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float)

    abstract fun changeDir()

    open fun jump() {
        if (!canJump) {
            return
        }

        body.applyLinearImpulse(
            /* impulse = */ Vector2(0f, JUMP_VELOCITY),
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
        protected val ANIMATION_SPEED = 360f

        protected const val ANIMATION_RANGE = 60f

        private const val HIT_RANGE = .5f

        private const val DAMAGE_TINT_TIMEOUT_S = 0.5f
        private val DAMAGE_TINT_COLOR = Color((0xff8080 shl 8) or 0xFF)

        private const val JUMP_VELOCITY = -3f

        private const val JUMP_COOLDOWN_MS = 500L
    }
}
