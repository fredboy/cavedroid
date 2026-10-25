package ru.fredboy.cavedroid.game.controller.explosion

import ru.fredboy.cavedroid.domain.items.model.block.Block

internal object CreeperDaylightRules {
    const val DESPAWN_SECONDS = 10f

    fun exposed(isDay: Boolean, headY: Int, lowestSurfaceY: Int, blockAt: (Int) -> Block): Boolean {
        if (!isDay || headY !in 0..lowestSurfaceY) return false
        return (headY downTo 0).all {
            val block = blockAt(it)
            block.isNone() || block.params.isTransparent || !block.params.castsShadows || !block.params.hasCollision
        }
    }

    fun elapsed(previous: Float, exposed: Boolean, delta: Float): Float = if (exposed) previous + delta else 0f
}
