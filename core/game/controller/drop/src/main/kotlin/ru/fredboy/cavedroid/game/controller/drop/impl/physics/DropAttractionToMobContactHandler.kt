package ru.fredboy.cavedroid.game.controller.drop.impl.physics

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindDropContactHandler
class DropAttractionToMobContactHandler @Inject constructor() : AbstractContactHandler<Drop, Mob>() {

    override val sensorType: ContactSensorType?
        get() = ContactSensorType.DROP_ATTRACTOR

    override val entityClassA: KClass<Drop>
        get() = Drop::class

    override val entityClassB: KClass<Mob>
        get() = Mob::class

    override fun Drop.handleBeginContact(contact: Contact, entityB: Mob) {
        if (entityB !is Player) {
            return
        }

        val toPlayer = entityB.position.cpy().sub(position)
        controlVector.set(toPlayer.nor().scl(50f))
    }

    override fun Drop.handleEndContact(contact: Contact, entityB: Mob) {
        controlVector.setZero()
    }
}
