package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld

class UpdateFenceActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val air = Block.None(params("none", collision = false))
    private val fore = mutableMapOf<Pair<Int, Int>, Block>()
    private val back = mutableMapOf<Pair<Int, Int>, Block>()
    private val variants = listOf("", "_left", "_right", "_both").associate { suffix ->
        val key = "fence_oak$suffix"
        key to Block.Normal(params(key, collision = suffix != "_both", fence = true))
    }
    private val action = UpdateFenceAction(world, GetBlockByKeyUseCase(repository))

    init {
        every { world.isChunkLoadedAt(any()) } returns true
        every { world.getForeMap(any(), any()) } answers { fore[firstArg<Int>() to secondArg<Int>()] ?: air }
        every { world.getBackMap(any(), any()) } answers { back[firstArg<Int>() to secondArg<Int>()] ?: air }
        every { world.setForeMap(any(), any(), any(), any(), any()) } answers {
            fore[firstArg<Int>() to secondArg<Int>()] = thirdArg()
        }
        every { world.setBackMap(any(), any(), any(), any(), any()) } answers {
            back[firstArg<Int>() to secondArg<Int>()] = thirdArg()
        }
        every { repository.getBlockByKey(any()) } answers { variants.getValue(firstArg()) }
    }

    @Test
    fun `row becomes passable inside and stable without oscillation`() {
        for (x in 0..2) fore[x to 4] = variants.getValue("fence_oak")
        repeat(3) { for (x in 0..2) action.update(x, 4) }
        assertEquals(listOf("fence_oak_right", "fence_oak_both", "fence_oak_left"), (0..2).map { fore.getValue(it to 4).params.key })
        assertFalse(fore.getValue(1 to 4).params.hasCollision)
        verify(exactly = 3) { world.setForeMap(any(), any(), any(), false, false) }
    }

    @Test
    fun `removing one side restores the central post collision`() {
        for (x in 0..2) fore[x to 4] = variants.getValue("fence_oak")
        action.update(1, 4)
        fore.remove(2 to 4)
        action.update(1, 4)
        assertEquals("fence_oak_left", fore.getValue(1 to 4).params.key)
        assertTrue(fore.getValue(1 to 4).params.hasCollision)
    }

    @Test
    fun `foreground and background connections are independent`() {
        fore[0 to 4] = variants.getValue("fence_oak")
        fore[-1 to 4] = variants.getValue("fence_oak")
        back[0 to 4] = variants.getValue("fence_oak")
        back[1 to 4] = variants.getValue("fence_oak")
        action.update(0, 4)
        assertEquals("fence_oak_left", fore.getValue(0 to 4).params.key)
        assertEquals("fence_oak_right", back.getValue(0 to 4).params.key)
        verify { world.setBackMap(0, 4, any(), false, false) }
    }

    @Test
    fun `unloaded neighbor preserves saved variant until it loads`() {
        fore[15 to 4] = variants.getValue("fence_oak_both")
        every { world.isChunkLoadedAt(16) } returns false
        action.update(15, 4)
        assertEquals("fence_oak_both", fore.getValue(15 to 4).params.key)
        every { world.isChunkLoadedAt(16) } returns true
        action.update(15, 4)
        assertEquals("fence_oak", fore.getValue(15 to 4).params.key)
    }

    private fun params(key: String, collision: Boolean, fence: Boolean = false): CommonBlockParams = mockk<CommonBlockParams>(relaxed = true).also {
        every { it.key } returns key
        every { it.hasCollision } returns collision
        every { it.collisionMargins } returns BlockInsets.Meters(0.375f, 0f, 0.375f, 0f)
        every { it.updateActionKey } returns FenceBlocks.UPDATE_ACTION_KEY.takeIf { fence }
        every { it.useActionKey } returns null
    }
}
