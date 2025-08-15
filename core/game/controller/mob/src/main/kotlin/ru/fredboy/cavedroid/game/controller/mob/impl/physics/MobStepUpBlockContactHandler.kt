package ru.fredboy.cavedroid.game.controller.mob.impl.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Contact
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.abstraction.AbstractContactHandler
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject
import kotlin.reflect.KClass

@GameScope
@BindMobContactHandler
class MobStepUpBlockContactHandler @Inject constructor() : AbstractContactHandler<Mob, Block>() {

    override val sensorType: ContactSensorType?
        get() = null

    override val entityClassA: KClass<Mob>
        get() = Mob::class

    override val entityClassB: KClass<Block>
        get() = Block::class

    override fun canHandleContact(
        contact: Contact,
        sensorType: ContactSensorType?,
        entityA: Any,
        entityB: Any,
    ): Boolean {
        if (!super.canHandleContact(contact, sensorType, entityA, entityB)) {
            return false
        }

        val blockBody = contact.fixtureA.body.takeIf { it.userData is Block }
            ?: contact.fixtureB.body.takeIf { it.userData is Block }
            ?: return false

        val block = entityB as? Block ?: return false

        with(entityA as Mob) {
            // on the ground or swimming
            if (!canJump && !swim) {
                return false
            }

            // moves
            if (controlVector.x == 0f && velocity.x == 0f) {
                return false
            }

            val vMobToBlock = blockBody.position.cpy().sub(position)

            // moves towards block
            if (!(velocity.x > 0 && vMobToBlock.x > 0 || velocity.x < 0 && vMobToBlock.x < 0)) {
                return false
            }

            val mobRect = hitbox
            val blockRect = block.getRectangle(blockBody.position.x.toInt(), blockBody.position.y.toInt())

            // not higher than half block
            if (mobRect.y + mobRect.height > blockRect.y + 0.5f) {
                return false
            }

            // safety margin
            if (mobRect.y + mobRect.height <= blockRect.y + 0.01f) {
                return false
            }
        }

        return true
    }

    override fun Mob.handleBeginContact(contact: Contact, entityB: Block) {
        contact.isEnabled = false

        val blockBody = contact.fixtureA.body.takeIf { it.userData is Block }
            ?: contact.fixtureB.body.takeIf { it.userData is Block }
            ?: run {
                Gdx.app.log(TAG, "Contact handler called with wrong entityB")
                return
            }

        val mobRect = hitbox
        val blockRect = entityB.getRectangle(blockBody.position.x.toInt(), blockBody.position.y.toInt())

        applyPendingTransform(Vector2(0f, blockRect.y - (mobRect.y + mobRect.height)))
    }

    override fun Mob.handleEndContact(contact: Contact, entityB: Block) = Unit

    companion object {
        private const val TAG = "MobStepUpBlockContactHandler"
    }
}
