package ru.fredboy.cavedroid.domain.items.model.block

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FenceBlocksTest {

    private val baseParams = CommonBlockParams(
        key = "stone",
        collisionMargins = BlockInsets.Meters(0f, 0f, 0f, 0f),
        hitPoints = 1,
        dropInfo = emptyList(),
        dropsSingleItem = true,
        hasCollision = true,
        isBackground = false,
        isTransparent = false,
        requiresBlock = false,
        allowAttachToNeighbour = false,
        animationInfo = null,
        texture = null,
        spriteMargins = BlockInsets.Pixels(0, 0, 0, 0),
        toolLevel = 0,
        toolType = null,
        damage = 0,
        tint = null,
        isFallable = false,
        castsShadows = true,
        lightInfo = null,
        replaceable = false,
        material = null,
        actionSoundKey = null,
        combustible = false,
        requiresBackground = false,
    )

    private val air = Block.None(baseParams.copy(key = "none", hasCollision = false))
    private val planks = Block.Normal(baseParams.copy(key = "planks_oak"))
    private val fence = Block.Normal(baseParams.copy(key = "fence_oak", updateActionKey = FenceBlocks.UPDATE_ACTION_KEY))

    @Test
    fun `connectors follow solid cell edges on each side`() {
        assertEquals("fence_oak", FenceBlocks.connectedKey(fence, air, air))
        assertEquals("fence_oak_left", FenceBlocks.connectedKey(fence, planks, air))
        assertEquals("fence_oak_right", FenceBlocks.connectedKey(fence, air, planks))
        assertEquals("fence_oak_both", FenceBlocks.connectedKey(fence, planks, planks))
    }

    @Test
    fun `inset cactus never attaches and transparency does not exclude glass`() {
        val cactus = Block.Normal(baseParams.copy(collisionMargins = BlockInsets.Meters(0.0625f, 0f, 0.0625f, 0f)))
        val glass = Block.Normal(baseParams.copy(isTransparent = true))
        assertEquals("fence_oak", FenceBlocks.connectedKey(fence, cactus, cactus))
        assertEquals("fence_oak_both", FenceBlocks.connectedKey(fence, glass, glass))
    }

    @Test
    fun `asymmetric collision connects only on the touching side`() {
        val leftEdge = Block.Normal(baseParams.copy(collisionMargins = BlockInsets.Meters(0f, 0f, 0.75f, 0f)))
        assertEquals("fence_oak_right", FenceBlocks.connectedKey(fence, leftEdge, leftEdge))
    }

    @Test
    fun `non colliding normal blocks never connect despite zero margins`() {
        val flower = Block.Normal(baseParams.copy(key = "flower", hasCollision = false))
        assertEquals("fence_oak", FenceBlocks.connectedKey(fence, flower, flower))
    }

    @Test
    fun `passable fences of another wood and open gates retain connections`() {
        val passable = Block.Normal(fence.params.copy(key = "fence_birch_both", hasCollision = false))
        val openGate = Block.Normal(baseParams.copy(key = "fence_gate_spruce_left_open", hasCollision = false, useActionKey = FenceBlocks.USE_GATE_ACTION_KEY))
        assertEquals("fence_oak_both", FenceBlocks.connectedKey(fence, passable, openGate))
    }

    @Test
    fun `removing a neighbor replaces rather than appends the previous suffix`() {
        for (suffix in listOf("", "_left", "_right", "_both")) {
            val connected = Block.Normal(fence.params.copy(key = "fence_birch$suffix"))
            assertEquals("fence_birch", FenceBlocks.connectedKey(connected, air, air))
            assertEquals("fence_birch_left", FenceBlocks.connectedKey(connected, planks, air))
        }
    }

    @Test
    fun `gate toggles retain wood and hinge direction`() {
        for (wood in listOf("oak", "spruce", "birch")) {
            for (side in listOf("left", "right")) {
                val closed = Block.Normal(baseParams.copy(key = "fence_gate_${wood}_${side}_closed"))
                val open = Block.Normal(baseParams.copy(key = "fence_gate_${wood}_${side}_open"))
                assertEquals(open.params.key, FenceBlocks.toggledGateKey(closed))
                assertEquals(closed.params.key, FenceBlocks.toggledGateKey(open))
            }
        }
    }
}
