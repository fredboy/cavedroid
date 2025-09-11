package ru.fredboy.cavedroid.entity.projectile.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.utils.Vector2Proxy
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.projectile.abstraction.ProjectileWorldAdapter
import kotlin.experimental.or

class Projectile(
    val item: Item,
    val damage: Int,
    val width: Float,
    val height: Float,
    val dropOnGround: Boolean,
) : Disposable {

    private var _body: Body? = null

    val body get() = requireNotNull(_body)

    val position: Vector2 get() = body.position

    val velocity = Vector2Proxy(
        getVelocity = { body.linearVelocity },
        setVelocity = { body.linearVelocity = it },
    )

    val hitbox: Rectangle get() = Rectangle(position.x - width / 2f, position.y - height / 2f, width, height)

    val isDead get() = isOnGround || hasHitMob

    var isOnGround = false

    var hasHitMob = false

    fun spawn(x: Float, y: Float, velocity: Vector2, world: World) {
        if (_body != null) {
            Gdx.app.error(TAG, "spawn called on projectile of type ${item.params.key} when body was already set!")
            return
        }

        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(x, y)
            bullet = true
        }

        _body = world.createBody(bodyDef)

        body.userData = this

        val bodyShape = PolygonShape().apply {
            setAsBox(width / 2f, height / 2f)
        }

        val fixtureDef = FixtureDef().apply {
            shape = bodyShape
            density = 1f
            friction = 1f
            restitution = 0f
            filter.categoryBits = PhysicsConstants.CATEGORY_PROJECTILE
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK or PhysicsConstants.CATEGORY_MOB
        }

        body.createFixture(fixtureDef)

        bodyShape.dispose()

        body.applyForceToCenter(velocity.cpy(), true)
    }

    fun getDropItem(): InventoryItem? {
        return (dropOnGround && !hasHitMob).ifTrue { item.toInventoryItem() }
    }

    fun update(projectileWorldAdapter: ProjectileWorldAdapter, delta: Float) {
        if (position.y > projectileWorldAdapter.height) {
            isOnGround = true
        }
    }

    override fun dispose() {
        body.world.destroyBody(body)
        _body = null
    }

    companion object {
        private const val TAG = "Projectile"
    }
}
