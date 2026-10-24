package ru.fredboy.cavedroid.game.controller.explosion

import com.badlogic.gdx.math.Rectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import kotlin.random.Random

class ExplosionRulesTest {
    private val air = ExplosionTestBlocks.air

    private fun blast(blockAt: (Int, Int) -> Block) = ExplosionRules.affectedBlocks(0.5f, 0.5f, random = Random(42), blockAt = blockAt)

    @Test
    fun `empty air has no block drops`() {
        assertTrue(blast { _, _ -> air }.isEmpty())
    }

    @Test
    fun `higher resistance produces smaller craters`() {
        val dirt = blast { _, _ -> ExplosionTestBlocks.dirt }
        val stone = blast { _, _ -> ExplosionTestBlocks.stone }
        assertTrue(dirt.size > stone.size)
        assertTrue(stone.isNotEmpty())
    }

    @Test
    fun `obsidian and bedrock survive and shield blocks behind them`() {
        for (shield in listOf(ExplosionTestBlocks.obsidian, ExplosionTestBlocks.bedrock)) {
            val blocks = blast { x, _ ->
                when {
                    x == 1 -> shield
                    x > 1 -> ExplosionTestBlocks.dirt
                    else -> air
                }
            }
            assertTrue(blocks.isEmpty())
        }
    }

    @Test
    fun `water at the origin absorbs terrain damage`() {
        val blocks = blast { x, y -> if (x == 0 && y == 0) ExplosionTestBlocks.water else ExplosionTestBlocks.dirt }
        assertTrue(blocks.isEmpty())
    }

    @Test
    fun `zero resistance TNT is affected at negative coordinates`() {
        val blocks = ExplosionRules.affectedBlocks(-4.5f, 4.5f, random = Random(42)) { x, y ->
            if (x == -6 && y == 4) ExplosionTestBlocks.tnt else air
        }
        assertTrue(-6 to 4 in blocks)
    }

    @Test
    fun `distant blocks remain intact`() {
        val blocks = blast { x, _ -> if (x == 10) ExplosionTestBlocks.dirt else air }
        assertTrue(blocks.isEmpty())
    }

    @Test
    fun `walls shield mob hitboxes before terrain is removed`() {
        val target = Rectangle(3f, 0f, 1f, 2f)
        assertEquals(1f, ExplosionRules.exposure(0.5f, 0.5f, target) { _, _ -> air })
        assertEquals(
            0f,
            ExplosionRules.exposure(0.5f, 0.5f, target) { x, _ ->
                if (x == 2) ExplosionTestBlocks.stone else air
            },
        )
    }

    @Test
    fun `partial cover reduces exposure and fluids do not shield mobs`() {
        val target = Rectangle(3f, 0f, 1f, 2f)
        val covered = ExplosionRules.exposure(0.5f, 0.5f, target) { x, y ->
            if (x == 2 && y == 0) ExplosionTestBlocks.stone else air
        }
        assertTrue(covered > 0f && covered < 1f)
        assertEquals(1f, ExplosionRules.exposure(0.5f, 0.5f, target) { _, _ -> ExplosionTestBlocks.water })
    }

    @Test
    fun `damage decreases with distance and exposure`() {
        assertEquals(57, ExplosionRules.damage(ExplosionRules.impact(0f, 1f)))
        assertEquals(22, ExplosionRules.damage(ExplosionRules.impact(4f, 1f)))
        assertEquals(1, ExplosionRules.damage(ExplosionRules.impact(4f, 0f)))
        assertEquals(0f, ExplosionRules.impact(9f, 1f))
        assertFalse(ExplosionRules.impact(4f, 0.5f) >= ExplosionRules.impact(4f, 1f))
    }

    @Test
    fun `looping worlds use the nearest blast while infinite coordinates never wrap`() {
        assertEquals(1f, ExplosionRules.horizontalDistance(255.5f, 0.5f, 256, false))
        assertEquals(-1f, ExplosionRules.horizontalDistance(0.5f, 255.5f, 256, false))
        assertEquals(-255f, ExplosionRules.horizontalDistance(255.5f, 0.5f, 256, true))
    }
}
