package ru.fredboy.cavedroid.entity.vehicle

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Fixture

/** A single-seat vehicle, independent of the mob hierarchy. */
class Vehicle(val id: String, val type: String, val body: Body) {
    var facing = 1
    var health = BoatTuning.MAX_HEALTH
        private set
    var occupied = false
    var passengerFixture: Fixture? = null
    var rowing = 0f
    var oarPhase = 0f
    var submerged = 0f
    var fireTime = 0f
    var fireDamageTimer = 0f
    var soundTimer = 0f
    var dropOnBreak = true

    val position get() = body.position
    val velocity get() = body.linearVelocity
    val hitbox get() = Rectangle(position.x - BoatTuning.WIDTH / 2f, position.y - BoatTuning.HEIGHT / 2f, BoatTuning.WIDTH, BoatTuning.HEIGHT)

    fun damage(amount: Int) {
        health = (health - amount.coerceIn(0, BoatTuning.MAX_HEALTH)).coerceAtLeast(0)
    }

    fun restoreHealth(value: Int) {
        health = value.coerceIn(1, BoatTuning.MAX_HEALTH)
    }

    fun snapshot() = VehicleState(id, type, position.x, position.y, velocity.x, velocity.y, facing, health)
}

/** Save boundary has no native resources. */
data class VehicleState(
    val id: String,
    val type: String = "oak_boat",
    val x: Float,
    val y: Float,
    val velocityX: Float = 0f,
    val velocityY: Float = 0f,
    val facing: Int = 1,
    val health: Int = BoatTuning.MAX_HEALTH,
)

interface VehiclePhysicsFactory {
    fun create(state: VehicleState): Vehicle
    fun setOccupied(vehicle: Vehicle, width: Float, height: Float, occupied: Boolean)
}
