package ru.fredboy.cavedroid.game.controller.projectile.impl.physics

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import ru.fredboy.cavedroid.entity.vehicle.Vehicle
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindProjectileContactHandler
class ProjectileToVehicleContactHandler @Inject constructor() : AbstractContactHandler<Projectile, Vehicle>() {

    override val sensorType: ContactSensorType? get() = null

    override val entityClassA: KClass<Projectile>
        get() = Projectile::class

    override val entityClassB: KClass<Vehicle>
        get() = Vehicle::class

    override fun Projectile.handleBeginContact(contact: Contact, entityB: Vehicle) {
        if (isDead) return
        entityB.damage(damage)
        hasHitMob = true
        onTargetHit?.invoke(this, entityB)
    }

    override fun Projectile.handleEndContact(contact: Contact, entityB: Vehicle) = Unit
}
