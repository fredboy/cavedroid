package ru.fredboy.cavedroid.game.controller.container

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter

class ChestJoiningTest {
    private val blocks = mutableMapOf<ContainerCoordinates, String>()
    private val unloaded = mutableSetOf<Int>()
    private var wrap = false
    private fun normalize(x: Int) = if (wrap) Math.floorMod(x, 16) else x
    private val world = mockk<ContainerWorldAdapter>(relaxed = true) {
        every { containerX(any()) } answers { normalize(firstArg()) }
        every { chestBlockKey(any(), any(), any()) } answers { blocks[ContainerCoordinates(normalize(firstArg()), secondArg(), thirdArg())] }
        every { setChestBlockKey(any(), any(), any(), any()) } answers {
            blocks[ContainerCoordinates(normalize(firstArg()), secondArg(), thirdArg())] = arg(3)
        }
        every { isContainerChunkLoaded(any()) } answers { firstArg<ContainerCoordinates>().x !in unloaded }
        every { checkContainerAtCoordinates(any(), any()) } returns true
    }
    private val factory = mockk<ContainerFactory> {
        every { createContainer(any(), any()) } answers { Chest(mockk(relaxed = true)) }
    }
    private val drops = mockk<DropAdapter>(relaxed = true)
    private val controller = ContainerController(mockk(), world, factory, drops, mockk())
    private val layer = Layer.BACKGROUND

    private fun place(x: Int, z: Layer = layer): Chest {
        blocks[ContainerCoordinates(normalize(x), 4, z.z)] = "chest"
        controller.onBlockPlaced(mockk<Block.Chest>(), normalize(x), 4, z)
        return controller.getContainer(x, 4, z.z) as Chest
    }

    @Test
    fun `joins in either placement order and opens in left to right order from either half`() {
        for (origin in listOf(0, 10)) {
            val right = place(origin + 1)
            val left = place(origin)
            for (x in origin..origin + 1) {
                val pair = requireNotNull(controller.getChestPair(x, 4, layer.z))
                assertSame(left, pair.first)
                assertSame(right, pair.second)
            }
            assertEquals("chest_large_left", blocks[ContainerCoordinates(origin, 4, layer.z)])
            assertEquals("chest_large_right", blocks[ContainerCoordinates(origin + 1, 4, layer.z)])
        }
    }

    @Test
    fun `third chest stays single and chests on different layers do not join`() {
        place(0)
        place(1)
        val third = place(2)
        assertNull(controller.getChestPair(2, 4, layer.z)?.second)
        assertSame(third, controller.getContainer(2, 4, layer.z))
        place(8, Layer.FOREGROUND)
        place(9)
        assertNull(controller.getChestPair(9, 4, layer.z)?.second)
    }

    @Test
    fun `breaking one half drops only its inventory and keeps survivor inventory`() {
        val left = place(0)
        val right = place(1)
        val item = mockk<ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem>()
        right.items[3] = item
        controller.onBlockDestroyed(mockk<Block.Chest>(), 0, 4, layer, true, true)
        blocks.remove(ContainerCoordinates(0, 4, layer.z))
        controller.onBlockPlaced(mockk<Block.None>(), 0, 4, layer)
        assertEquals("chest", blocks[ContainerCoordinates(1, 4, layer.z)])
        assertSame(item, right.items[3])
        assertSame(right, controller.getChestPair(1, 4, layer.z)?.first)
        assertNull(controller.getChestPair(1, 4, layer.z)?.second)
        verify(exactly = 1) { drops.dropInventory(0f, 4f, left.inventory) }
        verify(exactly = 0) { drops.dropInventory(any(), any(), right.inventory) }
    }

    @Test
    fun `restored parts keep their slots and an unloaded partner does not reset the block`() {
        val left = place(0)
        val right = place(1)
        controller.evictContainer(ContainerCoordinates(1, 4, layer.z))
        unloaded += 1
        controller.update(0f)
        assertEquals("chest_large_left", blocks[ContainerCoordinates(0, 4, layer.z)])
        unloaded.clear()
        val restored = Chest(mockk(relaxed = true), right.items.toList())
        controller.addContainer(1, 4, layer.z, restored)
        assertSame(left, controller.getChestPair(1, 4, layer.z)?.first)
        assertSame(restored, controller.getChestPair(0, 4, layer.z)?.second)
    }

    @Test
    fun `joining across looping world boundary uses canonical container coordinates`() {
        wrap = true
        val left = place(15)
        val right = place(16)
        assertSame(left, controller.getChestPair(0, 4, layer.z)?.first)
        assertSame(right, controller.getChestPair(15, 4, layer.z)?.second)
        assertSame(left, controller.getChestPair(-1, 4, layer.z)?.first)
    }
}
