package ru.fredboy.cavedroid.game.controller.mob.impl.physics

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindMobContactHandler
class MobToDropContactHandler @Inject constructor() : AbstractContactHandler<Mob, Drop>() {

    override val sensorType: ContactSensorType?
        get() = ContactSensorType.DROP_PICK_UP

    override val entityClassA: KClass<Mob>
        get() = Mob::class

    override val entityClassB: KClass<Drop>
        get() = Drop::class

    override fun Mob.handleBeginContact(contact: Contact, entityB: Drop) {
        // TODO: Other mobs should also pick drop
        if (this !is Player) {
            return
        }

        if (entityB.isPickedUp) {
            return
        }

        inventory.pickUpItem(entityB.inventoryItem).ifTrue {
            entityB.isPickedUp = true
        }
    }

    override fun Mob.handleEndContact(contact: Contact, entityB: Drop) = Unit
}
