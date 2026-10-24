package ru.fredboy.cavedroid.game.controller.explosion

import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.domain.items.model.block.Block
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Minecraft's blast attenuation and damage, with rays distributed around a 2D circle. */
object ExplosionRules {
    const val TNT_POWER = 4f
    const val FUSE_SECONDS = 4f
    const val EFFECT_SECONDS = 0.8f

    fun affectedBlocks(
        x: Float,
        y: Float,
        power: Float = TNT_POWER,
        random: Random = Random.Default,
        blockAt: (Int, Int) -> Block,
    ): Set<Pair<Int, Int>> {
        val affected = linkedSetOf<Pair<Int, Int>>()
        repeat(128) { ray ->
            val angle = ray * Math.PI * 2 / 128
            val dx = cos(angle).toFloat() * 0.3f
            val dy = sin(angle).toFloat() * 0.3f
            var rayX = x
            var rayY = y
            var strength = power * (0.7f + random.nextFloat() * 0.6f)
            while (strength > 0f) {
                val bx = floor(rayX).toInt()
                val by = floor(rayY).toInt()
                val block = blockAt(bx, by)
                if (!block.isNone()) {
                    if (block.params.hitPoints < 0 && !block.isFluid() && !block.isFire()) break
                    strength -= (block.params.blastResistance + 0.3f) * 0.3f
                    if (strength > 0f && !block.isFluid()) affected += bx to by
                }
                rayX += dx
                rayY += dy
                strength -= 0.225f
            }
        }
        return affected
    }

    /** Sample the target before removing terrain so walls shield entities from this blast. */
    fun exposure(x: Float, y: Float, target: Rectangle, blockAt: (Int, Int) -> Block): Float {
        var visible = 0
        for (ix in 0..2) {
            for (iy in 0..2) {
                val tx = target.x + target.width * (0.1f + ix * 0.4f)
                val ty = target.y + target.height * (0.1f + iy * 0.4f)
                val dx = tx - x
                val dy = ty - y
                val steps = ceil(sqrt(dx * dx + dy * dy) / 0.15f).toInt().coerceAtLeast(1)
                val clear = (0..steps).all { step ->
                    val px = x + dx * step / steps
                    val py = y + dy * step / steps
                    val bx = floor(px).toInt()
                    val by = floor(py).toInt()
                    val block = blockAt(bx, by)
                    val margins = block.params.collisionMargins
                    !block.params.hasCollision ||
                        px < bx + margins.left ||
                        px > bx + 1f - margins.right ||
                        py < by + margins.top ||
                        py > by + 1f - margins.bottom
                }
                if (clear) visible++
            }
        }
        return visible / 9f
    }

    fun impact(distance: Float, exposure: Float, power: Float = TNT_POWER): Float = (1f - distance / (2f * power)).coerceIn(0f, 1f) * exposure.coerceIn(0f, 1f)

    fun damage(impact: Float, power: Float = TNT_POWER): Int = ((impact * impact + impact) * 7f * power + 1f).toInt()

    fun horizontalDistance(from: Float, to: Float, worldWidth: Int, infinite: Boolean): Float {
        val distance = to - from
        if (infinite) return distance
        return distance - floor((distance + worldWidth / 2f) / worldWidth) * worldWidth
    }
}
