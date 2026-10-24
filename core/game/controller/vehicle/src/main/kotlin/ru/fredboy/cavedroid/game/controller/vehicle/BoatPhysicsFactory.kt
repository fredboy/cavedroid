package ru.fredboy.cavedroid.game.controller.vehicle

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.vehicle.BoatTuning
import ru.fredboy.cavedroid.entity.vehicle.Vehicle
import ru.fredboy.cavedroid.entity.vehicle.VehiclePhysicsFactory
import ru.fredboy.cavedroid.entity.vehicle.VehicleState
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.experimental.or

@GameScope
class BoatPhysicsFactory @Inject constructor(private val world: GameWorld) : VehiclePhysicsFactory {
    override fun create(state: VehicleState): Vehicle {
        val body = world.world.createBody(
            BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
                fixedRotation = true
                position.set(state.x, state.y)
            },
        )
        val vehicle = Vehicle(state.id, state.type, body)
        body.userData = vehicle
        val shape = PolygonShape().apply { setAsBox(BoatTuning.WIDTH / 2f, BoatTuning.HEIGHT / 2f) }
        body.createFixture(
            FixtureDef().apply {
                this.shape = shape
                density = 1f
                friction = 0.8f
                filter.categoryBits = PhysicsConstants.CATEGORY_VEHICLE
                filter.maskBits = PhysicsConstants.CATEGORY_BLOCK or PhysicsConstants.CATEGORY_VEHICLE or PhysicsConstants.CATEGORY_PROJECTILE
            },
        )
        shape.dispose()
        body.setLinearVelocity(state.velocityX, state.velocityY)
        vehicle.facing = if (state.facing < 0) -1 else 1
        vehicle.restoreHealth(state.health)
        return vehicle
    }

    override fun setOccupied(vehicle: Vehicle, width: Float, height: Float, occupied: Boolean) {
        vehicle.passengerFixture?.let(vehicle.body::destroyFixture)
        vehicle.passengerFixture = null
        vehicle.occupied = occupied
        if (!occupied) return
        val shape = PolygonShape().apply {
            setAsBox(width / 2f, height / 2f, Vector2(0f, BoatTuning.SEAT_OFFSET_Y), 0f)
        }
        vehicle.passengerFixture = vehicle.body.createFixture(
            FixtureDef().apply {
                this.shape = shape
                filter.categoryBits = PhysicsConstants.CATEGORY_VEHICLE
                filter.maskBits = PhysicsConstants.CATEGORY_BLOCK
                friction = 0f
            },
        )
        shape.dispose()
    }
}
