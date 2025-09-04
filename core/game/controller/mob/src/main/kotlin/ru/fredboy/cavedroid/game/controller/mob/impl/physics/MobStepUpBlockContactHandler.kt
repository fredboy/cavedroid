package ru.fredboy.cavedroid.game.controller.mob.impl.physics

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

        val block = contact.fixtureA.userData as? Block
            ?: contact.fixtureB.userData as? Block
            ?: return false

        with(entityA as Mob) {
            // on the ground or swimming
            if (!canJump && !climb) {
                return false
            }

            // moves
            if (controlVector.x == 0f) {
                return false
            }

            val (blockX, blockY) = getBlockCoordinates(contact)

            val blockRect = block.getRectangle(blockX, blockY)

            val vMobToBlock = blockRect.run { getCenter(Vector2()) }.cpy().sub(position)

            // moves towards block
            if (!(controlVector.x > 0 && vMobToBlock.x > 0 || controlVector.x < 0 && vMobToBlock.x < 0)) {
                return false
            }

            val mobRect = hitbox

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

        val (blockX, blockY) = getBlockCoordinates(contact)

        val mobRect = hitbox
        val blockRect = entityB.getRectangle(blockX, blockY)

        applyPendingTransform(Vector2(0f, blockRect.y - (mobRect.y + mobRect.height)))
    }

    override fun Mob.handleEndContact(contact: Contact, entityB: Block) = Unit

    private fun Mob.getBlockCoordinates(contact: Contact): Pair<Int, Int> {
        val horizontalPrecision = CONTACT_PRECISION * if (controlVector.x < 0f) -1 else 1
        return contact.worldManifold.points.first().let { vec ->
            (vec.x + horizontalPrecision).toInt() to (vec.y + CONTACT_PRECISION).toInt()
        }
    }

    companion object {
        private const val CONTACT_PRECISION = 0.01f
    }
}
