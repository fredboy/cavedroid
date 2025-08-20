package ru.fredboy.cavedroid.game.controller.mob.impl

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject

@GameScope
class MobPhysicsFactoryImpl @Inject constructor(
    private val mobWorldAdapter: MobWorldAdapter,
) : MobPhysicsFactory {

    override fun createBody(mob: Mob, x: Float, y: Float, physicsCategory: Short): Body {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(x, y)
            fixedRotation = true
        }

        val body = mobWorldAdapter.getBox2dWorld().createBody(bodyDef).apply {
            userData = mob
        }

        body.createMainBodyFixture(mob.width, mob.height, physicsCategory)
        body.createFeetFixtures(mob.width, mob.height, physicsCategory)
        body.createGroundSensor(mob.width, mob.height)
        body.createAutoJumpSensor(mob.height)

        return body
    }

    private fun Body.createMainBodyFixture(width: Float, height: Float, physicsCategory: Short) {
        val shapeHeight = if (height > width) {
            height - width
        } else {
            height - width / 2f
        }

        val bodyShape = createBoxShape(0f, 0f, width, shapeHeight)

        FixtureDef().apply {
            shape = bodyShape
            density = 1f
            friction = .2f
            restitution = 0f
            filter.categoryBits = physicsCategory
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }.also { fixtureDef ->
            createFixture(fixtureDef)
            bodyShape.dispose()
        }
    }

    private fun Body.createFeetFixtures(width: Float, height: Float, physicsCategory: Short) {
        if (height > width) {
            return createSingleFootFixture(width, height, physicsCategory)
        }

        val footRadius = width / 4f
        val leftFootX = -(width / 4f)
        val rightFootX = width / 4f
        val footY = height / 2f - footRadius

        val leftShape = createCircleShape(footRadius, leftFootX, footY)
        val rightShape = createCircleShape(footRadius, rightFootX, footY)

        FixtureDef().apply {
            friction = 8f
            restitution = 0f
            filter.categoryBits = physicsCategory
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }.also { fixtureDef ->
            fixtureDef.shape = leftShape
            createFixture(fixtureDef)
            leftShape.dispose()
        }.also { fixtureDef ->
            fixtureDef.shape = rightShape
            createFixture(fixtureDef)
            rightShape.dispose()
        }
    }

    fun Body.createSingleFootFixture(width: Float, height: Float, physicsCategory: Short) {
        val footRadius = width / 2f
        val footX = 0f
        val footY = height / 2f - footRadius

        val footShape = createCircleShape(footRadius, footX, footY)

        FixtureDef().apply {
            friction = 8f
            restitution = 0f
            filter.categoryBits = physicsCategory
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }.also { fixtureDef ->
            fixtureDef.shape = footShape
            createFixture(fixtureDef)
            footShape.dispose()
        }
    }

    private fun Body.createGroundSensor(width: Float, height: Float) {
        val sensorShape = createBoxShape(
            x = 0f,
            y = height / 2f + 0.0625f,
            width = width / 2f,
            height = 0.125f,
        )

        FixtureDef().apply {
            shape = sensorShape
            isSensor = true
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }.also { fixtureDef ->
            createFixture(fixtureDef).apply {
                userData = ContactSensorType.MOB_ON_GROUND
            }
            sensorShape.dispose()
        }
    }

    private fun Body.createAutoJumpSensor(height: Float) {
        val sensorShapeR = PolygonShape().apply {
            setAsBox(
                0.5f,
                0.125f,
                Vector2(0.5f, height / 2 - 0.8f),
                0f,
            )
        }

        val sensorShapeL = PolygonShape().apply {
            setAsBox(
                0.5f,
                0.125f,
                Vector2(-0.5f, height / 2 - 0.8f),
                0f,
            )
        }

        FixtureDef().apply {
            shape = sensorShapeR
            isSensor = true
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }.also { fixtureDef ->
            createFixture(fixtureDef).apply {
                userData = ContactSensorType.MOB_SHOULD_JUMP_RIGHT
            }
            sensorShapeR.dispose()
        }

        FixtureDef().apply {
            shape = sensorShapeL
            isSensor = true
            filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
        }.also { fixtureDef ->
            createFixture(fixtureDef).apply {
                userData = ContactSensorType.MOB_SHOULD_JUMP_LEFT
            }
            sensorShapeL.dispose()
        }
    }

    private fun createCircleShape(radius: Float, x: Float, y: Float): CircleShape {
        return CircleShape().apply {
            this.radius = radius
            position = Vector2(x, y)
        }
    }

    private fun createBoxShape(x: Float, y: Float, width: Float, height: Float): PolygonShape {
        return PolygonShape().apply {
            setAsBox(width / 2f, height / 2f, Vector2(x, y), 0f)
        }
    }

    private fun createEdgeShape(x1: Float, y1: Float, x2: Float, y2: Float): EdgeShape {
        return EdgeShape().apply {
            set(x1, y1, x2, y2)
        }
    }
}
