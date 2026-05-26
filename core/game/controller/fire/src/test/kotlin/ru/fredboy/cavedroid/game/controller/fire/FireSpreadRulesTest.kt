package ru.fredboy.cavedroid.game.controller.fire

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.item.isNone

class FireSpreadRulesTest {

    private fun block(
        isNone: Boolean = false,
        isFire: Boolean = false,
        combustible: Boolean = false,
    ): Block {
        val params = mockk<CommonBlockParams>()
        every { params.combustible } returns combustible
        return mockk {
            every { this@mockk.params } returns params
            every { this@mockk.isNone() } returns isNone
            every { this@mockk.isFire() } returns isFire
        }
    }

    @Test
    fun `combustible block can be ignited`() {
        assertTrue(FireSpreadRules.canIgnite(block(combustible = true)))
    }

    @Test
    fun `non-combustible block cannot be ignited`() {
        assertFalse(FireSpreadRules.canIgnite(block(combustible = false)))
    }

    @Test
    fun `None block cannot be ignited even if flag was true`() {
        assertFalse(FireSpreadRules.canIgnite(block(isNone = true, combustible = true)))
    }

    @Test
    fun `Fire block cannot be re-ignited`() {
        assertFalse(FireSpreadRules.canIgnite(block(isFire = true, combustible = true)))
    }

    @Test
    fun `shouldSpread is true below the spread chance threshold`() {
        val target = block(combustible = true)
        assertTrue(FireSpreadRules.shouldSpread(0f, target))
        assertTrue(FireSpreadRules.shouldSpread(FireSpreadRules.SPREAD_CHANCE - 0.01f, target))
    }

    @Test
    fun `shouldSpread is false at or above the spread chance threshold`() {
        val target = block(combustible = true)
        assertFalse(FireSpreadRules.shouldSpread(FireSpreadRules.SPREAD_CHANCE, target))
        assertFalse(FireSpreadRules.shouldSpread(0.99f, target))
    }

    @Test
    fun `shouldSpread is false on non-combustible regardless of roll`() {
        val target = block(combustible = false)
        assertFalse(FireSpreadRules.shouldSpread(0f, target))
    }

    @Test
    fun `support is valid only for combustible blocks`() {
        assertTrue(FireSpreadRules.supportStillValid(block(combustible = true)))
        assertFalse(FireSpreadRules.supportStillValid(block(combustible = false)))
    }
}
