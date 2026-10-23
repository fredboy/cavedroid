package ru.fredboy.cavedroid.domain.items.model.block

/** Fence variants are ordinary blocks sharing actions and a stable base item key. */
object FenceBlocks {

    const val UPDATE_ACTION_KEY = "update_fence"
    const val USE_GATE_ACTION_KEY = "use_fence_gate"

    fun isFence(block: Block): Boolean = block.params.updateActionKey == UPDATE_ACTION_KEY

    fun isGate(block: Block): Boolean = block.params.useActionKey == USE_GATE_ACTION_KEY

    fun connectedKey(fence: Block, left: Block, right: Block): String {
        val connectsLeft = connects(left, fromLeft = false)
        val connectsRight = connects(right, fromLeft = true)
        val suffix = when {
            connectsLeft && connectsRight -> "_both"
            connectsLeft -> "_left"
            connectsRight -> "_right"
            else -> ""
        }
        val baseKey = fence.params.key.removeSuffix("_left").removeSuffix("_right").removeSuffix("_both")
        return baseKey + suffix
    }

    fun toggledGateKey(gate: Block): String {
        val key = gate.params.key
        return if (key.endsWith("_open")) key.removeSuffix("_open") + "_closed" else key.removeSuffix("_closed") + "_open"
    }

    private fun connects(block: Block, fromLeft: Boolean): Boolean {
        if (isFence(block) || isGate(block)) return true
        val margin = if (fromLeft) block.params.collisionMargins.left else block.params.collisionMargins.right
        return block.params.hasCollision && block.width > 0f && block.height > 0f && margin <= 0f
    }
}
