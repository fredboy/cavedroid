package ru.fredboy.cavedroid.gameplay.physics.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import kotlin.random.Random

class ChunkCollisionMesherTest {

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
    private val air = Block.None(baseParams.copy(key = "none", hasCollision = false, castsShadows = false))
    private val stone = Block.Normal(baseParams)
    private val dirt = Block.Normal(baseParams.copy(key = "dirt"))
    private val slab = Block.Normal(baseParams.copy(key = "slab", collisionMargins = BlockInsets.Meters(0f, 0.5f, 0f, 0f)))
    private val inset = Block.Normal(baseParams.copy(key = "inset", collisionMargins = BlockInsets.Meters(0.125f, 0.25f, 0.125f, 0.25f)))
    private val insetAir = Block.Normal(inset.params.copy(key = "inset_air", hasCollision = false))
    private val mesher = ChunkCollisionMesher(air)
    private val legacy = LegacyCollisionMesher(air)

    @Test
    fun `empty sky retains user data without creating fixtures`() {
        val result = mesher.prepare(snapshot { _, _ -> air })
        assertTrue(result.fixtures.isEmpty())
        assertEquals(256, result.userData.blocks.size)
        assertTrue(result.userData.blocks.values.all { it === air })
    }

    @Test
    fun `enclosed air retains its cave contour`() {
        val input = snapshot { x, y -> if (x in 3..12 && y in 3..12) air else stone }
        val result = mesher.prepare(input)
        assertEquals(2, result.fixtures.size)
        assertTrue(result.fixtures.any { it.block === air })
        assertEquivalent(input)
    }

    @Test
    fun `inset air uses raw margins while user data uses fallback`() {
        val input = snapshot { x, y -> if (x in 3..12 && y in 3..12) insetAir else stone }
        assertEquivalent(input)
        val result = mesher.prepare(input)
        assertEquals(air, result.userData[-13, 131])
    }

    @Test
    fun `material boundaries diagonal contact and margins preserve ordered contours`() {
        val patterns = listOf<(Int, Int) -> Block>(
            { _, _ -> stone },
            { x, y -> if ((x + y) % 2 == 0) stone else dirt },
            { x, y -> if ((x + y) % 2 == 0) stone else air },
            { x, y ->
                if (y > 8) {
                    stone
                } else if (x % 3 == 0) {
                    slab
                } else if (x % 3 == 1) {
                    inset
                } else {
                    air
                }
            },
            { x, y -> if ((x in 2..5 && y in 2..5) || (x in 6..9 && y in 6..9)) stone else air },
        )
        patterns.forEach { assertEquivalent(snapshot(block = it)) }
    }

    @Test
    fun `random terrain preserves fixtures at negative and large origins and mirror offsets`() {
        val random = Random(92728)
        val palette = listOf(air, stone, dirt, slab, inset, insetAir)
        repeat(100) { iteration ->
            val x = listOf(-32, 0, 1008, 16_777_216)[iteration % 4]
            val input = snapshot(x = x) { _, _ -> palette[random.nextInt(palette.size)] }
            for (offset in listOf(0, -1024, 1024)) assertEquivalent(input, offset)
        }
    }

    @Test
    fun `reusing scratch cannot change an earlier prepared result`() {
        val input = snapshot { _, _ -> stone }
        val result = mesher.prepare(input)
        val vertices = result.fixtures.single().vertices.copyOf()
        mesher.prepare(snapshot { x, y -> if ((x + y) % 2 == 0) inset else air })
        assertEquals(vertices.toList(), result.fixtures.single().vertices.toList())
    }

    private fun snapshot(x: Int = -16, y: Int = 128, block: (Int, Int) -> Block): CollisionSnapshot = CollisionSnapshot(x, y, Array(256) { block(it / 16, it % 16) })

    private fun assertEquivalent(input: CollisionSnapshot, offset: Int = 0) {
        val expected = legacy.prepare(input, offset)
        val actual = mesher.prepare(input, offset)
        assertEquals(expected.userData, actual.userData)
        assertEquals(expected.fixtures.size, actual.fixtures.size)
        expected.fixtures.zip(actual.fixtures).forEach { (old, new) ->
            assertEquals(old.block, new.block)
            assertEquals(old.vertices.map(Float::toBits), new.vertices.map(Float::toBits))
        }
    }
}
