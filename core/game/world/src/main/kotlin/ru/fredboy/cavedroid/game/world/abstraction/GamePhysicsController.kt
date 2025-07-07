package ru.fredboy.cavedroid.game.world.abstraction

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.world.GameWorld

abstract class GamePhysicsController : Disposable {

    private var _gameWorld: GameWorld? = null

    protected val gameWorld get() = requireNotNull(_gameWorld)

    protected abstract fun Mob.pickUpDrop(drop: Drop)

    protected abstract fun Drop.setAttractionTarget(mob: Mob)

    protected abstract fun Drop.resetAttractionTarget()

    protected abstract fun Mob.onTouchGround()

    protected abstract fun Mob.onShouldJump()

    protected abstract fun Drop.onTouchGround()

    protected abstract fun Mob.onLeaveGround()

    protected abstract fun Drop.onLeaveGround()

    fun attachToGameWorld(gameWorld: GameWorld) {
        _gameWorld = gameWorld
        gameWorld.world.setContactListener(ContactListenerImpl())
    }

    override fun dispose() {
        gameWorld.world.setContactListener(null)
        _gameWorld = null
    }

    private inner class ContactListenerImpl : ContactListener {
        override fun beginContact(contact: Contact?) {
            contact ?: return

            // no contacts between sensors
            if (contact.fixtureA.isSensor && contact.fixtureB.isSensor) {
                return
            }

            val mob = contact.fixtureA.body.userData as? Mob
                ?: contact.fixtureB.body.userData as? Mob

            val block = contact.fixtureA.body.userData as? Block
                ?: contact.fixtureB.body.userData as? Block

            val drop = contact.fixtureA.body.userData as? Drop
                ?: contact.fixtureB.body.userData as? Drop

            val sensorType = contact.fixtureA.userData as? ContactSensorType
                ?: contact.fixtureB.userData as? ContactSensorType

            when {
                sensorType == ContactSensorType.MOB_ON_GROUND && mob != null && block != null ->
                    mob.onTouchGround()

                sensorType == ContactSensorType.MOB_SHOULD_JUMP && mob != null && block != null ->
                    mob.onShouldJump()

                sensorType == ContactSensorType.DROP_ON_GROUND && drop != null && block != null ->
                    drop.onTouchGround()

                sensorType == ContactSensorType.DROP_ATTRACTOR && drop != null && mob != null ->
                    drop.setAttractionTarget(mob)

                sensorType == ContactSensorType.DROP_PICK_UP && mob != null && drop != null ->
                    mob.pickUpDrop(drop)
            }
        }

        override fun endContact(contact: Contact?) {
            contact ?: return

            // no contacts between sensors
            if (contact.fixtureA.isSensor && contact.fixtureB.isSensor) {
                return
            }

            val mob = contact.fixtureA.body.userData as? Mob
                ?: contact.fixtureB.body.userData as? Mob

            val block = contact.fixtureA.body.userData as? Block
                ?: contact.fixtureB.body.userData as? Block

            val drop = contact.fixtureA.body.userData as? Drop
                ?: contact.fixtureB.body.userData as? Drop

            val sensorType = contact.fixtureA.userData as? ContactSensorType
                ?: contact.fixtureB.userData as? ContactSensorType

            when {
                sensorType == ContactSensorType.MOB_ON_GROUND && mob != null && block != null ->
                    mob.onLeaveGround()

                sensorType == ContactSensorType.DROP_ON_GROUND && drop != null && block != null ->
                    drop.onLeaveGround()

                sensorType == ContactSensorType.DROP_ATTRACTOR && drop != null && mob != null ->
                    drop.resetAttractionTarget()
            }
        }

        override fun preSolve(contact: Contact?, oldManifold: Manifold?) = Unit

        override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit
    }
}
