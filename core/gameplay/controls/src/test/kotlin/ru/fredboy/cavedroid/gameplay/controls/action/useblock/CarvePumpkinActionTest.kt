package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class CarvePumpkinActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController> { every { player } returns this@CarvePumpkinActionTest.player }
    private val repository = mockk<ItemsRepository>()
    private val drops = mockk<DropController>(relaxed = true)
    private val action = CarvePumpkinAction(world, mobs, repository, drops)
    private val pumpkin = block("pumpkin")
    private val carved = block("carved_pumpkin")
    private val seeds = Item.Normal(mockk(), mockk())

    init {
        every { world.getForeMap(5, 10) } returns pumpkin
        every { repository.getBlockByKey("carved_pumpkin") } returns carved
        every { repository.getItemByKey("pumpkin_seeds") } returns seeds
        every { player.activeItem } returns InventoryItem(mockk<Item.Shears>(relaxed = true))
    }

    @Test
    fun `shears carve pumpkin and drop four seeds with one durability use`() {
        assertTrue(action.perform(pumpkin, 5, 10))

        verify(exactly = 1) { world.setForeMap(5, 10, carved) }
        verify(exactly = 1) { drops.addDrop(5.5f, 10.5f, match { it.item === seeds && it.amount == 4 }, null) }
        verify(exactly = 1) { player.durateActiveDurable() }
    }

    @Test
    fun `background pumpkins are carved in their own layer`() {
        every { world.getForeMap(5, 10) } returns Block.None(mockk(relaxed = true))
        every { world.getBackMap(5, 10) } returns pumpkin

        assertTrue(action.perform(pumpkin, 5, 10))

        verify(exactly = 1) { world.setBackMap(5, 10, carved) }
        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `bare hands and already carved pumpkins do not produce seeds`() {
        every { player.activeItem } returns InventoryItem(Item.Normal(mockk(), mockk()))
        assertFalse(action.perform(pumpkin, 5, 10))
        every { player.activeItem } returns InventoryItem(mockk<Item.Shears>(relaxed = true))
        assertFalse(action.perform(carved, 5, 10))

        verify(exactly = 0) { drops.addDrop(any(), any(), any<InventoryItem>(), any()) }
        verify(exactly = 0) { player.durateActiveDurable() }
    }

    @Test
    fun `stale interaction cannot carve a replaced block`() {
        every { world.getForeMap(5, 10) } returns carved

        assertFalse(action.perform(pumpkin, 5, 10))

        verify(exactly = 0) { player.durateActiveDurable() }
    }

    private fun block(key: String): Block.Normal = Block.Normal(
        mockk<CommonBlockParams>(relaxed = true) { every { this@mockk.key } returns key },
    )
}
