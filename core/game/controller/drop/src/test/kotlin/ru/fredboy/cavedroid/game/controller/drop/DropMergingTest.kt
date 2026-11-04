package ru.fredboy.cavedroid.game.controller.drop

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.assets.repository.DropSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.drop.model.Drop

class DropMergingTest {

    @Test
    fun `overlapping stacks of the same item key merge into one drop`() {
        val target = drop(amount = 10, x = 0.9f)
        val source = drop(amount = 20, x = 1.1f)

        assertEquals(setOf(source), mergeNearbyDrops(listOf(target, source)))
        assertEquals(30, target.amount)
        assertEquals(0, source.amount)
    }

    @Test
    fun `overflow remains in a separate drop`() {
        val target = drop(amount = 60)
        val source = drop(amount = 10)

        assertTrue(mergeNearbyDrops(listOf(target, source)).isEmpty())
        assertEquals(64, target.amount)
        assertEquals(6, source.amount)
    }

    @Test
    fun `one source fills multiple partial stacks without losing items`() {
        val left = drop(amount = 60, x = -0.3f)
        val right = drop(amount = 60, x = 0.3f)
        val source = drop(amount = 10)

        assertTrue(mergeNearbyDrops(listOf(left, right, source)).isEmpty())
        assertEquals(listOf(64, 64, 2), listOf(left.amount, right.amount, source.amount))
    }

    @Test
    fun `large piles preserve the total and obey the item stack limit`() {
        val drops = List(1000) { drop(amount = 1, maxStack = 16) }
        val consumed = mergeNearbyDrops(drops)
        val remaining = drops.filterNot { it in consumed }

        assertEquals(63, remaining.size)
        assertEquals(1000, remaining.sumOf { it.amount })
        assertTrue(remaining.all { it.amount in 1..16 })
    }

    @Test
    fun `different items and durability values remain separate`() {
        val drops = listOf(drop(key = "stone"), drop(key = "wood"), drop(key = "stone", durability = 2))

        assertTrue(mergeNearbyDrops(drops).isEmpty())
        assertTrue(drops.all { it.amount == 1 })
    }

    @Test
    fun `full and unstackable drops remain separate`() {
        val drops = listOf(drop(amount = 64), drop(maxStack = 1), drop(maxStack = 1))

        assertTrue(mergeNearbyDrops(drops).isEmpty())
        assertEquals(listOf(64, 1, 1), drops.map { it.amount })
    }

    @Test
    fun `nearby drops with disjoint hitboxes do not merge`() {
        val target = drop()
        val source = drop(x = 0.6f)

        assertTrue(mergeNearbyDrops(listOf(target, source)).isEmpty())
    }

    @Test
    fun `overlapping hitboxes merge across negative cell boundaries`() {
        val target = drop(x = -0.5f, y = -0.4f)
        val source = drop(x = -0.5f, y = 0f)

        assertEquals(setOf(source), mergeNearbyDrops(listOf(target, source)))
        assertEquals(2, target.amount)
    }

    @Test
    fun `hitboxes touching only at an edge or corner do not merge`() {
        for ((x, y) in listOf(0.5f to 0f, 0f to 0.5f, 0.5f to 0.5f)) {
            val target = drop()
            val source = drop(x = x, y = y)

            assertTrue(mergeNearbyDrops(listOf(target, source)).isEmpty())
            assertEquals(1, target.amount)
            assertEquals(1, source.amount)
        }
    }

    @Test
    fun `picked up drops cannot donate or receive items`() {
        val pickedUp = drop(pickedUp = true)
        val active = drop()

        assertTrue(mergeNearbyDrops(listOf(pickedUp, active)).isEmpty())
        assertTrue(mergeNearbyDrops(listOf(active, pickedUp)).isEmpty())
        assertEquals(1, pickedUp.amount)
        assertEquals(1, active.amount)
    }

    @Test
    fun `looping worlds merge across the seam`() {
        val target = drop(x = 1023.875f)
        val source = drop(x = 0.125f)

        assertEquals(setOf(source), mergeNearbyDrops(listOf(target, source), worldWidth = 1024))
        assertEquals(2, target.amount)
    }

    @Test
    fun `infinite worlds do not wrap distant drops`() {
        val target = drop(x = 1023.75f)
        val source = drop(x = 0.25f)

        assertTrue(mergeNearbyDrops(listOf(target, source)).isEmpty())
    }

    @Test
    fun `controller removes merged bodies without playing pickup sounds`() {
        val target = drop()
        val source = drop()
        val world = mockk<DropWorldAdapter>(relaxed = true) {
            every { isInfinite } returns true
        }
        val sounds = mockk<DropSoundAssetsRepository>(relaxed = true)
        val controller = DropController(world, DropQueue(), listOf(target, source), mockk(), mockk(), sounds, mockk())

        controller.update(1f / 60f)

        assertEquals(listOf(target), controller.getAllDrop().toList())
        assertEquals(2, target.amount)
        verifyOrder {
            source.isPickedUp = true
            source.dispose()
        }
        verify(exactly = 0) { target.dispose() }
        verify(exactly = 0) { sounds.getDropPopSound() }
    }

    private fun drop(
        amount: Int = 1,
        x: Float = 0f,
        y: Float = 0f,
        key: String = "stone",
        maxStack: Int = 64,
        durability: Int = 1,
        pickedUp: Boolean = false,
    ): Drop {
        val item = mockk<Item.Normal> {
            every { params.key } returns key
            every { params.maxStack } returns maxStack
        }
        val inventoryItem = InventoryItem(item, amount, durability)
        return mockk<Drop>(relaxed = true) {
            every { this@mockk.item } returns item
            every { this@mockk.inventoryItem } returns inventoryItem
            every { this@mockk.amount } answers { inventoryItem.amount }
            every { position } returns Vector2(x, y)
            every { hitbox } answers { Rectangle(x - Drop.DROP_SIZE / 2f, y - Drop.DROP_SIZE / 2f, Drop.DROP_SIZE, Drop.DROP_SIZE) }
            every { isPickedUp } returns pickedUp
            every { timestamp } returns TimeUtils.millis()
        }
    }
}
