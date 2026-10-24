package ru.fredboy.cavedroid.game.controller.explosion

import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.utils.Vector2Proxy
import ru.fredboy.cavedroid.domain.assets.repository.BlockActionSoundAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.drop.DropAmount
import ru.fredboy.cavedroid.domain.items.model.drop.DropInfo
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.impl.CreeperFuse
import ru.fredboy.cavedroid.entity.mob.model.CreeperMob
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

class ExplosionControllerTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val mobs = mockk<MobController>()
    private val player = mockk<Player>(relaxed = true)
    private val fire = mockk<FireController>()
    private val drops = DropQueue()
    private val items = mockk<GetItemByKeyUseCase>()
    private val sounds = mockk<BlockActionSoundAssetsRepository>()
    private val soundPlayer = mockk<SoundPlayer>(relaxed = true)
    private val saves = mockk<SaveDataRepository>()
    private val application = mockk<ApplicationContextRepository>(relaxed = true)
    private val context = mockk<GameContextRepository>(relaxed = true)
    private val foreground = mutableMapOf<Pair<Int, Int>, Block>()
    private val background = mutableMapOf<Pair<Int, Int>, Block>()
    private val air = ExplosionTestBlocks.air

    @BeforeEach
    fun setup() {
        every { world.height } returns 256
        every { world.width } returns 256
        every { world.isInfinite } returns true
        every { world.isChunkLoadedAt(any()) } returns true
        every { world.getForeMap(any(), any()) } answers {
            foreground[firstArg<Int>() to secondArg<Int>()] ?: if (secondArg<Int>() == 11) ExplosionTestBlocks.bedrock else air
        }
        every { world.getBackMap(any(), any()) } answers { background[firstArg<Int>() to secondArg<Int>()] ?: air }
        every { world.destroyForeMap(any(), any(), false, false) } answers {
            foreground.remove(firstArg<Int>() to secondArg<Int>())
            Unit
        }
        every { world.destroyBackMap(any(), any(), false, false) } answers {
            background.remove(firstArg<Int>() to secondArg<Int>())
            Unit
        }
        every { mobs.player } returns player
        every { mobs.mobs } returns emptyList()
        every { player.gameMode } returns GameMode.CREATIVE
        every { player.position } returns Vector2(30f, 10f)
        every { fire.snapshot() } returns emptyList()
        every { sounds.getBlockActionSound(any()) } returns null
    }

    private fun controller() = ExplosionController(world, mobs, fire, drops, items, sounds, soundPlayer, saves, application, context)

    @Test
    fun `ignition removes TNT without a drop and cannot prime it twice`() {
        foreground[-2 to 10] = ExplosionTestBlocks.tnt
        val controller = controller()
        assertTrue(controller.prime(-2, 10))
        assertFalse(controller.prime(-2, 10))
        assertEquals(4f, controller.primedTnt.single().fuse)
        assertTrue(drops.queue.isEmpty())
        assertFalse(controller.prime(3, 10))
    }

    @Test
    fun `TNT explodes once after four simulated seconds`() {
        foreground[0 to 10] = ExplosionTestBlocks.tnt
        val controller = controller()
        controller.prime(0, 10)
        repeat(79) { controller.update(0.05f) }
        assertTrue(controller.effects.isEmpty())
        controller.update(0.06f)
        assertTrue(controller.primedTnt.isEmpty())
        assertEquals(1, controller.effects.size)
        controller.update(1f)
        assertTrue(controller.effects.isEmpty())
        verify(exactly = 1) { sounds.getBlockActionSound("explosion") }
    }

    @Test
    fun `blast primes TNT in both layers with short fuses instead of dropping it`() {
        foreground[1 to 10] = ExplosionTestBlocks.tnt
        background[-1 to 10] = ExplosionTestBlocks.tnt
        val controller = controller()
        controller.explode(0.5f, 10.5f)
        assertEquals(2, controller.primedTnt.size)
        assertTrue(controller.primedTnt.all { it.fuse in 0.5f..1.5f })
        assertTrue(foreground.isEmpty() && background.isEmpty())
        assertTrue(drops.queue.isEmpty())
    }

    @Test
    fun `underwater blast preserves terrain and nearby unprimed TNT`() {
        foreground[0 to 10] = ExplosionTestBlocks.water
        foreground[1 to 10] = ExplosionTestBlocks.tnt
        background[1 to 10] = ExplosionTestBlocks.dirt
        val controller = controller()
        controller.explode(0.5f, 10.5f)
        assertTrue(controller.primedTnt.isEmpty())
        assertEquals(2, foreground.size)
        assertEquals(1, background.size)
        assertEquals(1, controller.effects.size)
    }

    @Test
    fun `fire primes background TNT`() {
        background[0 to 10] = ExplosionTestBlocks.tnt
        every { fire.snapshot() } returns listOf(FireController.FireInstance(0, 10, Layer.BACKGROUND))
        val controller = controller()
        controller.update(0f)
        assertEquals(1, controller.primedTnt.size)
        assertTrue(background.isEmpty())
    }

    @Test
    fun `unloaded TNT pauses and resumes without losing its fuse`() {
        foreground[0 to 10] = ExplosionTestBlocks.tnt
        val controller = controller()
        controller.prime(0, 10)
        every { world.isChunkLoadedAt(any()) } returns false
        controller.update(10f)
        assertEquals(4f, controller.primedTnt.single().fuse)
        every { world.isChunkLoadedAt(any()) } returns true
        controller.update(1f)
        assertEquals(3f, controller.primedTnt.single().fuse, 0.001f)
    }

    @Test
    fun `saved TNT restores its position velocity and remaining fuse`() {
        foreground[-2 to 10] = ExplosionTestBlocks.tnt
        val original = controller()
        original.prime(-2, 10)
        original.update(1f)
        val snapshot = original.snapshot()
        every { context.isLoadGame() } returns true
        every { saves.loadTntEntries(any(), any()) } returns snapshot
        val restored = controller()
        assertEquals(snapshot, restored.snapshot())
        restored.update(0.5f)
        assertEquals(snapshot.single().fuse - 0.5f, restored.snapshot().single().fuse, 0.001f)
        assertEquals(snapshot, original.snapshot())
    }

    @Test
    fun `blast drops blocks that require a pickaxe without using the held tool`() {
        val drop = mockk<InventoryItem>()
        val item = mockk<Item.Normal>()
        every { items["cobblestone"] } returns item
        every { item.toInventoryItem(1, 1) } returns drop
        val params = ExplosionTestBlocks.params("stone", resistance = 6f).copy(
            toolType = Item.Pickaxe::class.java,
            toolLevel = 1,
            dropInfo = listOf(DropInfo("cobblestone", true, DropAmount.ExactAmount(1))),
        )
        foreground[0 to 10] = Block.Normal(params)
        controller().explode(0.5f, 10.5f)
        assertTrue(foreground.isEmpty())
        assertEquals(drop, drops.queue.single().item)
    }

    @Test
    fun `survival player takes blast damage and outward knockback`() {
        every { player.gameMode } returns GameMode.SURVIVAL
        every { player.position } returns Vector2(4.5f, 8.5f)
        every { player.width } returns 0.6f
        every { player.height } returns 1.8f
        var velocity = Vector2()
        every { player.velocity } returns Vector2Proxy({ velocity }, { velocity = it })
        controller().explode(0.5f, 8.5f)
        verify(exactly = 1) { player.damage(22) }
        assertEquals(10f, velocity.x, 0.001f)
    }

    @Test
    fun `nearby player across the looping seam is damaged once`() {
        every { world.isInfinite } returns false
        every { player.gameMode } returns GameMode.SURVIVAL
        every { player.position } returns Vector2(0.5f, 8.5f)
        every { player.width } returns 0.6f
        every { player.height } returns 1.8f
        var velocity = Vector2()
        every { player.velocity } returns Vector2Proxy({ velocity }, { velocity = it })
        controller().explode(255.5f, 8.5f)
        verify(exactly = 1) { player.damage(46) }
        assertTrue(velocity.x > 0f)
    }

    private fun creeper(): CreeperMob {
        val fuse = CreeperFuse()
        val creeper = mockk<CreeperMob>(relaxed = true)
        every { creeper.fuse } returns fuse
        every { creeper.position } returns Vector2(0.5f, 8.5f)
        every { creeper.mapX } returns 0
        every { creeper.upperMapY } returns 7
        var daylight = 0f
        every { creeper.daylightSec } answers { daylight }
        every { creeper.daylightSec = any() } answers { daylight = firstArg() }
        val list = mutableListOf<ru.fredboy.cavedroid.entity.mob.model.Mob>(creeper)
        every { mobs.mobs } returns list
        every { mobs.removeMob(creeper) } answers {
            list.remove(creeper)
            Unit
        }
        every { world.generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 42)
        return creeper
    }

    @Test
    fun `surface creepers disappear after ten daylight seconds without loot or explosions`() {
        val creeper = creeper()
        every { world.isDayTime() } returns true
        val controller = controller()
        controller.update(9f)
        verify(exactly = 0) { mobs.removeMob(creeper) }
        controller.update(1f)
        verify(exactly = 1) {
            mobs.removeMob(creeper)
            creeper.dispose()
        }
        assertTrue(controller.effects.isEmpty())
        assertTrue(drops.queue.isEmpty())
    }

    @Test
    fun `ready creeper is removed before a smaller explosion and does not drop death loot`() {
        val creeper = creeper()
        creeper.fuse.elapsedSec = CreeperFuse.DURATION_SEC
        val controller = controller()
        controller.update(0f)
        controller.update(0f)
        verify(exactly = 1) {
            mobs.removeMob(creeper)
            creeper.dispose()
        }
        verify(exactly = 1) { sounds.getBlockActionSound("explosion") }
        assertEquals(1, controller.effects.size)
        assertTrue(drops.queue.isEmpty())
    }

    @Test
    fun `creeper power limits damage radius to six blocks`() {
        every { player.gameMode } returns GameMode.SURVIVAL
        every { player.position } returns Vector2(7.5f, 8.5f)
        controller().explode(0.5f, 8.5f, CreeperFuse.EXPLOSION_POWER)
        verify(exactly = 0) { player.damage(any()) }
    }

    @Test
    fun `creative player receives neither damage nor knockback`() {
        every { player.position } returns Vector2(0.5f, 10.5f)
        controller().explode(0.5f, 10.5f)
        verify(exactly = 0) { player.damage(any()) }
        verify(exactly = 0) { player.velocity }
    }
}
