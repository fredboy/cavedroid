package ru.fredboy.cavedroid.entity.drop.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.utils.Vector2Proxy
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import kotlin.math.abs

class Drop(
    val inventoryItem: InventoryItem,
) : Disposable {

    constructor(item: Item, amount: Int = 1) : this(InventoryItem(item, amount))

    private var _body: Body? = null

    val body: Body get() = requireNotNull(_body)

    val position: Vector2 get() = body.position

    val velocity = Vector2Proxy(
        getVelocity = { body.linearVelocity },
        setVelocity = { body.linearVelocity = it },
    )

    val hitbox: Rectangle
        get() = Rectangle(
            position.x - DROP_SIZE / 2f,
            position.y - DROP_SIZE / 2f,
            DROP_SIZE,
            DROP_SIZE,
        )

    var isPickedUp = false

    val item get() = inventoryItem.item

    val amount get() = inventoryItem.amount

    var isBobbing = false

    var bobTime = 0f

    val controlVector = Vector2()

    fun spawn(x: Float, y: Float, world: World) {
        if (_body != null) {
            Gdx.app.error(
                /* tag = */ TAG,
                /* message = */
                "Attempted to drop ${inventoryItem.item::class.simpleName} on coordinates ($x;$y), " +
                    "when is already spawned and alive!",
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

        val bodyShape = PolygonShape().apply {
            setAsBox(DROP_SIZE / 2f, DROP_SIZE / 2f)
        }

        val sensorShape = CircleShape().apply {
            radius = MAGNET_DISTANCE
        }

        val bodyFixtureDef = FixtureDef().apply {
            shape = bodyShape
            density = 1f
            friction = .2f
            restitution = 0f
            filter.categoryBits = PHYSICS_CATEGORY
            filter.maskBits = Block.PHYSICS_CATEGORY
        }

        val pickUpFixtureDef = FixtureDef().apply {
            shape = bodyShape
            isSensor = true
        }

        val sensorFixtureDef = FixtureDef().apply {
            shape = sensorShape
            isSensor = true
        }

        body.createFixture(bodyFixtureDef)
        body.createFixture(pickUpFixtureDef).apply {
            userData = "pick_up_sensor"
        }
        body.createFixture(sensorFixtureDef).apply {
            userData = "drop_sensor"
        }

        bodyShape.dispose()
        sensorShape.dispose()
    }

    fun update(dropWorldAdapter: DropWorldAdapter, delta: Float) {
        if (isBobbing) {
            bobTime += delta
            if (bobTime >= MathUtils.PI) {
                bobTime = 0f
            }
        }

        if (!controlVector.isZero) {
            body.applyForceToCenter(controlVector, true)
            velocity.x = MathUtils.clamp(velocity.x, -abs(controlVector.x), abs(controlVector.x))
            velocity.y = MathUtils.clamp(velocity.y, -abs(controlVector.y), abs(controlVector.y))
        }

        val overlappingBlock = dropWorldAdapter.getForegroundBlock(
            x = position.x.toInt(),
            y = position.y.toInt(),
        )

        val overlappingRect = overlappingBlock.getRectangle(position.x.toInt(), position.y.toInt())

        if (controlVector.isZero && overlappingBlock.params.hasCollision && hitbox.overlaps(overlappingRect)) {
            body.setTransform(position.x, overlappingRect.y - DROP_SIZE / 2f, 0f)
        }

        if (position.x > dropWorldAdapter.width) {
            body.setTransform(Vector2(position.x - dropWorldAdapter.width, position.y), 0f)
        } else if (position.x < 0) {
            body.setTransform(Vector2(position.x + dropWorldAdapter.width, position.y), 0f)
        }

        if (position.y > dropWorldAdapter.height) {
            isPickedUp = true
        }
    }

    override fun dispose() {
        body.world.destroyBody(body)
        _body = null
    }

    companion object {
        private const val TAG = "Drop"
        private const val MAGNET_DISTANCE = 1f
        const val DROP_SIZE = .5f

        const val PHYSICS_CATEGORY: Short = 0x04
    }
}
