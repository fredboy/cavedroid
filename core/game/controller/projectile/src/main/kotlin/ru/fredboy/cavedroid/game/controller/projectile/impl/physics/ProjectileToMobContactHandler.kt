package ru.fredboy.cavedroid.game.controller.projectile.impl.physics

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindProjectileContactHandler
class ProjectileToMobContactHandler @Inject constructor() : AbstractContactHandler<Projectile, Mob>() {

    override val sensorType: ContactSensorType? get() = null

    override val entityClassA: KClass<Projectile>
        get() = Projectile::class

    override val entityClassB: KClass<Mob>
        get() = Mob::class

    override fun Projectile.handleBeginContact(contact: Contact, entityB: Mob) {
        entityB.damage(damage)
        hasHitMob = true
    }

    override fun Projectile.handleEndContact(contact: Contact, entityB: Mob) = Unit
}
