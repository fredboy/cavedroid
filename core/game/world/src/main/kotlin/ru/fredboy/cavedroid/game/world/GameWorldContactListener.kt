package ru.fredboy.cavedroid.game.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ChunkUserData
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

class GameWorldContactListener @Inject constructor(
    private val contactHandlers: Set<@JvmSuppressWildcards AbstractContactHandler<*, *>>,
) : Disposable {

    private var _gameWorld: GameWorld? = null

    private val gameWorld get() = requireNotNull(_gameWorld)

    fun attachToGameWorld(gameWorld: GameWorld) {
        _gameWorld = gameWorld
        gameWorld.world.setContactListener(ContactListenerImpl())
    }

    private fun findContactHandler(contact: Contact): Triple<AbstractContactHandler<Any, Any>, Any, Any>? {
        return contactHandlers
            .mapNotNull { handler ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    handler as AbstractContactHandler<Any, Any>
                } catch (e: ClassCastException) {
                    Gdx.app.error(TAG, "Exception handler cast", e)
                    null
                }
            }
            .flatMap { handler ->
                val entityA = (contact.fixtureA.body.userData as? ChunkUserData)
                    ?.let { contact.fixtureA.userData } ?: contact.fixtureA.body.userData
                val entityB = (contact.fixtureB.body.userData as? ChunkUserData)
                    ?.let { contact.fixtureB.userData } ?: contact.fixtureB.body.userData

                listOf(
                    Triple(handler, entityA, entityB),
                    Triple(handler, entityB, entityA),
                )
            }
            .firstOrNull { (handler, entityA, entityB) ->
                val sensorType = contact.fixtureA.userData as? ContactSensorType
                    ?: contact.fixtureB.userData as? ContactSensorType

                handler.canHandleContact(contact, sensorType, entityA, entityB)
            }
    }

    override fun dispose() {
        gameWorld.world.setContactListener(null)
        _gameWorld = null
    }

    private inner class ContactListenerImpl : ContactListener {
        override fun beginContact(contact: Contact?) {
            contact ?: return

            if (!contact.isTouching) {
                return
            }

            // no contacts between sensors
            if (contact.fixtureA.isSensor && contact.fixtureB.isSensor) {
                return
            }

            val mob = contact.fixtureA.body.userData as? Mob
                ?: contact.fixtureB.body.userData as? Mob

            val block = contact.fixtureA.userData as? Block
                ?: contact.fixtureB.userData as? Block

            val sensorType = contact.fixtureA.userData as? ContactSensorType
                ?: contact.fixtureB.userData as? ContactSensorType

            // disable friction while mob is moving
            if (mob != null && block != null && !mob.controlVector.isZero) {
                contact.friction = 0f
            }

            // fall damage
            if (sensorType == null && mob != null && block != null) {
                mob.damage(max((mob.velocity.y.pow(2f) / (gameWorld.world.gravity.y * 2f)).roundToInt() - 3, 0))
            }

            val (handler, entityA, entityB) = findContactHandler(contact) ?: return
            handler.handleBeginContact(contact, entityA, entityB)
        }

        override fun endContact(contact: Contact?) {
            contact ?: return

            // no contacts between sensors
            if (contact.fixtureA.isSensor && contact.fixtureB.isSensor) {
                return
            }

            val (handler, entityA, entityB) = findContactHandler(contact) ?: return
            handler.handleEndContact(contact, entityA, entityB)
        }

        override fun preSolve(contact: Contact?, oldManifold: Manifold?) = Unit

        override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit
    }

    companion object {
        private const val TAG = "GamePhysicsController"
    }
}
