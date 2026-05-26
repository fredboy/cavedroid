package ru.fredboy.cavedroid.game.controller.fire

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.isNone

/**
 * Pure decision functions for fire ignition and spreading. Kept free of libGDX
 * types so it can be unit-tested without a GL context.
 */
object FireSpreadRules {

    const val SPREAD_CHANCE = 0.15f

    /**
     * A block can be ignited if it is combustible and not already on fire / air.
     * Fluids and other special blocks are excluded by [Block.params.combustible]
     * (only authored combustible blocks have the flag set in JSON).
     */
    fun canIgnite(target: Block): Boolean {
        if (target.isNone() || target.isFire()) return false
        return target.params.combustible
    }

    /**
     * Roll-aware variant: returns true when [random] (uniform in [0, 1)) falls
     * within the per-tick spread chance for [target].
     */
    fun shouldSpread(random: Float, target: Block): Boolean = canIgnite(target) && random < SPREAD_CHANCE

    /**
     * A fire cell stays valid as long as the supporting block at the same
     * coordinates is still combustible. Once the support has been destroyed or
     * replaced (e.g., by an explosion or by another fire's burn-down), the
     * fire above it should be extinguished.
     */
    fun supportStillValid(support: Block): Boolean = support.params.combustible
}
