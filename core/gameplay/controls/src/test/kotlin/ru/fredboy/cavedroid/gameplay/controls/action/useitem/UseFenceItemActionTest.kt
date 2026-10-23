package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.api.OnboardingEvents
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.model.SpriteOrigin
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.items.model.item.CommonItemParams
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.PlaceBlockItemToBackgroundAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.PlaceBlockItemToForegroundAction

class UseFenceItemActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController> {
        every { player } returns this@UseFenceItemActionTest.player
        every { mobs } returns emptyList()
    }
    private val stats = mockk<StatsRepository>(relaxed = true)
    private val onboarding = mockk<OnboardingEvents>(relaxed = true)
    private val soundPlayer = mockk<SoundPlayer>(relaxed = true)
    private val sounds = mockk<StepsSoundAssetsRepository>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val getBlock = GetBlockByKeyUseCase(repository)
    private val itemParams = CommonItemParams("fence_birch", "Birch Fence", SpriteOrigin(0f, 1f), 64, 15000L, null, null)
    private val item = mockk<Item.Usable> { every { params } returns itemParams }
    private val fence = block("fence_birch", true, fence = true)
    private val both = block("fence_birch_both", false, fence = true)
    private val air = block("none", false)
    private val planks = block("planks_birch", true)
    private val action = UseFenceItemAction(
        getBlock,
        PlaceBlockItemToForegroundAction(world, mockk(), mobs, sounds, soundPlayer, stats, getBlock),
        PlaceBlockItemToBackgroundAction(world, mobs, sounds, soundPlayer, stats),
        onboarding,
    )

    init {
        every { repository.getBlockByKey("fence_birch") } returns fence
        every { repository.getBlockByKey("fence_birch_both") } returns both
        every { world.getForeMap(any(), any()) } returns air
        every { player.hitbox } returns Rectangle(5f, 5f, 1f, 2f)
    }

    @Test
    fun `usable fence cannot place a solid post inside the player`() {
        assertFalse(action.perform(item, 5, 5))
        verify(exactly = 0) { world.placeToForeground(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
    }

    @Test
    fun `usable fence between solid neighbors places the passable variant`() {
        every { world.getForeMap(4, 5) } returns planks
        every { world.getForeMap(6, 5) } returns planks
        every { world.placeToForeground(5, 5, both, false, false) } returns true
        assertTrue(action.perform(item, 5, 5))
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
        verify(exactly = 1) { stats.recordBlockPlaced() }
        verify(exactly = 1) { onboarding.notifyPlace() }
    }

    @Test
    fun `background use places the same wood behind the player`() {
        every { world.placeToBackground(5, 5, fence, false, false) } returns true
        assertTrue(action.performBackground(item, 5, 5))
        verify(exactly = 1) { world.placeToBackground(5, 5, fence, false, false) }
        verify(exactly = 0) { world.placeToForeground(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
        verify(exactly = 1) { stats.recordBlockPlaced() }
    }

    @Test
    fun `failed background placement keeps inventory and statistics`() {
        every { world.placeToBackground(5, 5, fence, false, false) } returns false
        assertFalse(action.performBackground(item, 5, 5))
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
        verify(exactly = 0) { stats.recordBlockPlaced() }
    }

    private fun block(key: String, collision: Boolean, fence: Boolean = false): Block.Normal {
        val params = mockk<CommonBlockParams>(relaxed = true)
        every { params.key } returns key
        every { params.hasCollision } returns collision
        every { params.updateActionKey } returns FenceBlocks.UPDATE_ACTION_KEY.takeIf { fence }
        every { params.useActionKey } returns null
        every { params.material } returns null
        every { params.collisionMargins } returns BlockInsets.Meters(if (fence) 0.375f else 0f, 0f, if (fence) 0.375f else 0f, 0f)
        return Block.Normal(params)
    }
}
