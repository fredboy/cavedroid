package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.items.model.block.Block

internal fun canSpiderClimbWall(hitbox: Rectangle, movementX: Float, getBlock: (Int, Int) -> Block): Boolean {
    if (movementX == 0f) return false

    // Exclude the feet so a floor or a low step is not mistaken for a wall.
    val probe = Rectangle(
        if (movementX > 0f) hitbox.x + hitbox.width else hitbox.x - WALL_REACH,
        hitbox.y + hitbox.height * 0.2f,
        WALL_REACH,
        hitbox.height * 0.5f,
    )
    var touchesWall = false
    forEachBlockInArea(probe) { x, y ->
        val block = getBlock(x, y)
        if (block.params.hasCollision && block.getRectangle(x, y).overlaps(probe)) {
            touchesWall = true
        }
    }
    return touchesWall
}

private const val WALL_REACH = 0.08f
