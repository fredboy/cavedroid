package ru.fredboy.cavedroid.gameplay.physics.impl

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Provider
import kotlin.random.Random

class MobSpawnerAdapterImplTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val controller = mockk<MobController>(relaxed = true)
    private val factory = mockk<MobFactory>(relaxed = true)
    private val params = mockk<MobParams>(relaxed = true)
    private val repository = mockk<MobParamsRepository>()
    private val air = mockk<Block>(relaxed = true)
    private val solid = mockk<Block>(relaxed = true)
    private val coordinates = ContainerCoordinates(10, 180, 0)
    private val adapter = MobSpawnerAdapterImpl(Provider { world }, Provider { controller }, factory, repository)

    init {
        every { world.isInfinite } returns true
        every { world.height } returns 256
        every { world.isChunkLoadedAt(any()) } returns true
        every { world.getForeMap(any(), any()) } answers { if (secondArg<Int>() >= 181) solid else air }
        every { solid.params.hasCollision } returns true
        every { solid.getRectangle(any(), any()) } answers { Rectangle(firstArg<Int>().toFloat(), secondArg<Int>().toFloat(), 1f, 1f) }
        every { params.width } returns 1f
        every { params.height } returns 1f
        every { params.maxSpawnLight } returns 0.5f
        every { repository.getMobParamsByKey(any()) } returns params
        every { controller.mobs } returns emptyList()
        every { controller.player.hitbox } returns Rectangle(25f, 180f, 1f, 2f)
        every { controller.player.position } returns Vector2(15f, 180f)
    }

    @Test
    fun `any registered mob key is passed to the factory`() {
        for (key in listOf("spider", "cow", "skeleton", "custom_mob")) {
            assertTrue(adapter.trySpawn(coordinates, key, Random(42)))
            verify(exactly = 1) { factory.create(any(), any(), key) }
        }
    }

    @Test
    fun `invalid keys and unloaded chunks never spawn`() {
        every { repository.getMobParamsByKey("missing") } returns null
        assertFalse(adapter.trySpawn(coordinates, "missing", Random(42)))
        every { world.isChunkLoadedAt(any()) } returns false
        assertFalse(adapter.trySpawn(coordinates, "spider", Random(42)))
        verify(exactly = 0) { factory.create(any(), any(), any()) }
    }

    @Test
    fun `light obstructions and fluids prevent spawning`() {
        every { world.getLightAt(any(), any()) } returns 1f
        assertFalse(adapter.trySpawn(coordinates, "spider", Random(42)))
        every { world.getLightAt(any(), any()) } returns 0f
        every { air.params.hasCollision } returns true
        every { air.getRectangle(any(), any()) } answers { Rectangle(firstArg<Int>().toFloat(), secondArg<Int>().toFloat(), 1f, 1f) }
        assertFalse(adapter.trySpawn(coordinates, "spider", Random(42)))
        every { air.params.hasCollision } returns false
        every { air.isFluid() } returns true
        assertFalse(adapter.trySpawn(coordinates, "spider", Random(42)))
        verify(exactly = 0) { factory.create(any(), any(), any()) }
    }

    @Test
    fun `nearby population includes pending spawns and caps the wave`() {
        val mob = mockk<Mob>(relaxed = true)
        every { mob.params.key } returns "spider"
        every { mob.position } returns Vector2(12f, 180f)
        every { controller.flushPendingSpawns(any()) } answers {
            every { controller.mobs } returns List(6) { mob }
        }
        assertFalse(adapter.trySpawn(coordinates, "spider", Random(42)))
        verify(exactly = 0) { factory.create(any(), any(), any()) }
    }

    @Test
    fun `activation respects depth death and looping seam`() {
        assertTrue(adapter.isPlayerNearby(coordinates))
        every { controller.player.position } returns Vector2(10f, 100f)
        assertFalse(adapter.isPlayerNearby(coordinates))
        every { world.isInfinite } returns false
        every { world.width } returns 1024
        every { controller.player.position } returns Vector2(1020f, 180f)
        assertTrue(adapter.isPlayerNearby(coordinates))
        every { controller.player.isDead } returns true
        assertFalse(adapter.isPlayerNearby(coordinates))
    }
}
