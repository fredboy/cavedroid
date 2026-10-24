package ru.fredboy.cavedroid.game.controller.explosion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block

class TntPhysicsTest {
    private val air = ExplosionTestBlocks.air

    @Test
    fun `primed TNT initially jumps then falls under gravity`() {
        val tnt = PrimedTnt(0.5f, 4.5f, 0f, -4f, 4f)
        TntPhysics.move(tnt, 0.05f) { _, _ -> air }
        assertTrue(tnt.y < 4.5f)
        repeat(20) { TntPhysics.move(tnt, 0.05f) { _, _ -> air } }
        assertTrue(tnt.y > 4.5f)
    }

    @Test
    fun `fast falling TNT lands on the surface without tunnelling`() {
        val tnt = PrimedTnt(0.5f, 0.5f, 0f, 32f, 4f)
        TntPhysics.move(tnt, 0.5f) { _, y -> if (y == 3) ExplosionTestBlocks.stone else air }
        assertEquals(3f - TntPhysics.HALF_SIZE, tnt.y, 0.001f)
        assertEquals(0f, tnt.velocityY)
    }

    @Test
    fun `bottom slab collision uses its top margin`() {
        val slab = Block.Normal(ExplosionTestBlocks.params("slab", top = 0.5f))
        val tnt = PrimedTnt(-2.5f, 0.5f, 0f, 32f, 4f)
        TntPhysics.move(tnt, 0.5f) { _, y -> if (y == 3) slab else air }
        assertEquals(3.5f - TntPhysics.HALF_SIZE, tnt.y, 0.001f)
    }

    @Test
    fun `walls stop horizontal motion`() {
        val tnt = PrimedTnt(0.5f, 0.5f, 20f, 0f, 4f)
        TntPhysics.move(tnt, 0.2f) { x, _ -> if (x == 2) ExplosionTestBlocks.stone else air }
        assertEquals(2f - TntPhysics.HALF_SIZE, tnt.x, 0.001f)
        assertEquals(0f, tnt.velocityX)
    }

    @Test
    fun `liquids do not act as solid floors`() {
        val tnt = PrimedTnt(0.5f, 0.5f, 0f, 10f, 4f)
        TntPhysics.move(tnt, 0.2f) { _, _ -> ExplosionTestBlocks.water }
        assertTrue(tnt.y > 2f)
    }
}
