package ru.fredboy.cavedroid.ux.physics.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.world.abstraction.GamePhysicsController
import javax.inject.Inject

@GameScope
internal class GamePhysics @Inject constructor() : GamePhysicsController() {
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
        // TODO: auto jump
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
}
