package ru.fredboy.cavedroid.game.controller.explosion

import ru.fredboy.cavedroid.domain.items.model.block.Block
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/** Small swept steps keep primed TNT from tunnelling through blocks, including slabs. */
internal object TntPhysics {
    const val HALF_SIZE = 0.49f

    fun move(tnt: PrimedTnt, delta: Float, blockAt: (Int, Int) -> Block) {
        tnt.velocityY = (tnt.velocityY + 16f * delta).coerceAtMost(32f)
        val steps = ceil(maxOf(abs(tnt.velocityX), abs(tnt.velocityY)) * delta / 0.1f).toInt().coerceAtLeast(1)
        val step = delta / steps
        repeat(steps) {
            moveAxis(tnt, tnt.velocityX * step, horizontal = true, blockAt)
            moveAxis(tnt, tnt.velocityY * step, horizontal = false, blockAt)
        }
        val drag = 0.98f.pow(delta * 20f)
        tnt.velocityX *= drag
        tnt.velocityY *= drag
    }

    private fun moveAxis(tnt: PrimedTnt, distance: Float, horizontal: Boolean, blockAt: (Int, Int) -> Block) {
        if (distance == 0f) return
        var x = tnt.x + if (horizontal) distance else 0f
        var y = tnt.y + if (horizontal) 0f else distance
        var collided = false
        for (bx in floor(x - HALF_SIZE).toInt()..floor(x + HALF_SIZE).toInt()) {
            for (by in floor(y - HALF_SIZE).toInt()..floor(y + HALF_SIZE).toInt()) {
                val block = blockAt(bx, by)
                if (!block.params.hasCollision) continue
                val margins = block.params.collisionMargins
                val left = bx + margins.left
                val right = bx + 1f - margins.right
                val top = by + margins.top
                val bottom = by + 1f - margins.bottom
                if (x + HALF_SIZE <= left ||
                    x - HALF_SIZE >= right ||
                    y + HALF_SIZE <= top ||
                    y - HALF_SIZE >= bottom
                ) {
                    continue
                }
                collided = true
                if (horizontal) {
                    x = if (distance > 0f) left - HALF_SIZE else right + HALF_SIZE
                } else {
                    y = if (distance > 0f) top - HALF_SIZE else bottom + HALF_SIZE
                }
            }
        }
        tnt.x = x
        tnt.y = y
        if (collided) {
            if (horizontal) {
                tnt.velocityX = 0f
            } else {
                tnt.velocityY = 0f
                tnt.velocityX *= 0.7f
            }
        }
    }
}
