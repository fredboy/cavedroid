package ru.fredboy.cavedroid.ux.physics.impl

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.world.abstraction.GamePhysicsController
import javax.inject.Inject

@GameScope
internal class GamePhysicsControllerImpl @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : GamePhysicsController() {
    override fun Mob.pickUpDrop(drop: Drop) {
        (this as? Player)?.inventory?.pickUpItem(drop.inventoryItem)?.ifTrue {
            drop.isPickedUp = true
        }
    }

    override fun Drop.setAttractionTarget(mob: Mob) {
        if (mob !is Player) {
            return
        }
        val toPlayer = mob.position.cpy().sub(position)
        controlVector.set(toPlayer.nor().scl(50f))
    }

    override fun Drop.resetAttractionTarget() {
        controlVector.setZero()
    }

    override fun Mob.onTouchGround() {
        footContactCounter++
        isFlyMode = false
        controlVector.y = 0f
    }

    override fun Mob.onShouldJump() {
        if (this is Player && !applicationContextRepository.isAutoJumpEnabled()) {
            return
        }

        if (controlVector.x != 0f) {
            jump()
        }
    }

    override fun Drop.onTouchGround() {
        isBobbing = true
    }

    override fun Mob.onLeaveGround() {
        footContactCounter--
    }

    override fun Drop.onLeaveGround() {
        isBobbing = false
    }

    override fun Mob.stepUpTheBlock(block: Block, blockBody: Body): Boolean {
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

        applyPendingTransform(Vector2(0f, blockRect.y - (mobRect.y + mobRect.height)))

        return true
    }

    companion object {
        private const val TAG = "GamePhysicsControllerImpl"
    }
}
