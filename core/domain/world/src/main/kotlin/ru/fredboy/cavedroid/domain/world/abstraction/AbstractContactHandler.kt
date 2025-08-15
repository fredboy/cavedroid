package ru.fredboy.cavedroid.domain.world.abstraction

import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import kotlin.reflect.KClass

abstract class AbstractContactHandler<EntityA : Any, EntityB : Any> {

    abstract val sensorType: ContactSensorType?

    abstract val entityClassA: KClass<EntityA>
    abstract val entityClassB: KClass<EntityB>

    open fun canHandleContact(contact: Contact, sensorType: ContactSensorType?, entityA: Any, entityB: Any): Boolean {
        return sensorType == this.sensorType && entityClassA.isInstance(entityA) && entityClassB.isInstance(entityB)
    }

    protected abstract fun EntityA.handleBeginContact(contact: Contact, entityB: EntityB)

    protected abstract fun EntityA.handleEndContact(contact: Contact, entityB: EntityB)

    fun handleBeginContact(contact: Contact, entityA: EntityA, entityB: EntityB) {
        entityA.handleBeginContact(contact, entityB)
    }

    fun handleEndContact(contact: Contact, entityA: EntityA, entityB: EntityB) {
        entityA.handleEndContact(contact, entityB)
    }
}
