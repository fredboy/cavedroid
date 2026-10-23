package ru.fredboy.cavedroid.data.save.streaming

import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.save.model.ChunkEntitiesSaveData
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class ChunkEntityStreamerTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val mobs = mutableListOf<Mob>()
    private val mobController = mockk<MobController>(relaxed = true)
    private val drops = mockk<DropController>(relaxed = true)
    private val containers = mockk<ContainerController>(relaxed = true)
    private val saves = mockk<SaveDataRepository>(relaxed = true)
    private val app = mockk<ApplicationContextRepository>(relaxed = true)
    private val context = mockk<GameContextRepository>(relaxed = true)
    private val resident = mutableSetOf<Int>()
    private val savedMob = mob()
    private val snapshots = mutableListOf<List<Mob>>()
    private val factory = mockk<ContainerFactory>(relaxed = true)
    private val streamer = ChunkEntityStreamer(world, mobController, drops, containers, saves, app, context, mockk(), mockk(), factory)

    init {
        every { world.isInfinite } returns true
        every { world.forEachLoadedChunk(any()) } answers {
            resident.toList().forEach(firstArg<(Int) -> Unit>())
        }
        every { mobController.mobs } answers { mobs }
        every { mobController.addMob(any()) } answers {
            mobs.add(firstArg())
            Unit
        }
        every { mobController.removeMob(any()) } answers {
            mobs.remove(firstArg<Mob>())
            Unit
        }
        every { drops.getAllDrop() } returns emptyList()
        every { containers.containerMap } returns mutableMapOf()
        every { saves.loadChunkEntities(any(), any(), any(), any(), any()) } returns
            ChunkEntitiesSaveData(listOf(savedMob), emptyList(), emptyMap())
        every { saves.saveChunkEntities(any(), any(), any(), any(), any(), any()) } answers {
            snapshots.add(arg<List<Mob>>(3).toList())
            Unit
        }
        streamer.initialize()
    }

    @Test
    fun `save before readiness retains stored and newly arrived entities without duplication`() {
        load()
        val arriving = mob()
        mobs.add(arriving)
        streamer.flushAll()
        streamer.flushAll()
        streamer.onChunkBodiesReady(2)
        assertEquals(listOf(arriving, savedMob), mobs)
        assertEquals(listOf(mobs, mobs), snapshots)
        verify(exactly = 1) { saves.loadChunkEntities(any(), any(), 2, any(), any()) }
    }

    @Test
    fun `eviction before readiness persists unread entities then disposes them`() {
        load()
        resident.clear()
        streamer.onChunkUnloaded(2)
        streamer.onChunkBodiesReady(2)
        assertEquals(listOf(listOf(savedMob)), snapshots)
        assertEquals(emptyList<Mob>(), mobs)
        verify(exactly = 1) { savedMob.dispose() }
        verify(exactly = 1) { saves.loadChunkEntities(any(), any(), 2, any(), any()) }
    }

    @Test
    fun `reloaded chunk restores once per residence`() {
        load()
        streamer.onChunkBodiesReady(2)
        streamer.onChunkBodiesReady(2)
        streamer.onChunkUnloaded(2)
        load()
        streamer.onChunkBodiesReady(2)
        assertEquals(listOf(savedMob), mobs)
        verify(exactly = 2) { saves.loadChunkEntities(any(), any(), 2, any(), any()) }
    }

    @Test
    fun `missing snapshot is still considered restored`() {
        every { saves.loadChunkEntities(any(), any(), any(), any(), any()) } returns null
        load()
        streamer.onChunkBodiesReady(2)
        streamer.flushAll()
        streamer.onChunkBodiesReady(2)
        verify(exactly = 1) { saves.loadChunkEntities(any(), any(), 2, any(), any()) }
    }

    @Test
    fun `eviction includes queued spawns and leaves other chunks pending`() {
        load()
        val queued = mutableListOf(33f to mob(), -0.5f to mob())
        val spawned = queued.first().second
        every { mobController.flushPendingSpawns(any()) } answers {
            val accepts = firstArg<(Float) -> Boolean>()
            val selected = queued.filter { accepts(it.first) }
            selected.forEach { mobs.add(it.second) }
            queued.removeAll(selected.toSet())
        }
        streamer.onChunkUnloaded(2)
        assertEquals(listOf(listOf(savedMob, spawned)), snapshots)
        assertEquals(listOf(-0.5f), queued.map { it.first })
        assertEquals(emptyList<Mob>(), mobs)
        verifyOrder {
            mobController.flushPendingSpawns(any())
            drops.flushPendingSpawns(any())
            saves.saveChunkEntities(any(), any(), 2, any(), any(), any())
            spawned.dispose()
        }
    }

    @Test
    fun `full save flushes pending spawns before snapshot without evicting`() {
        load()
        val queued = mob()
        every { mobController.flushPendingSpawns(any()) } answers {
            mobs.add(queued)
            Unit
        }
        streamer.flushAll()
        assertEquals(listOf(listOf(savedMob, queued)), snapshots)
        verify(exactly = 0) { queued.dispose() }
        verify(exactly = 1) { drops.flushPendingSpawns(any()) }
    }

    @Test
    fun `generated container initializes once and saved empty chest is not refilled`() {
        val chestBlock = Block.Chest(mockk(relaxed = true), "mineshaft")
        val chest = mockk<Container>()
        val coordinate = ContainerCoordinates(33, 1, 1)
        val live = mutableMapOf<ContainerCoordinates, Container>()
        every { containers.containerMap } returns live
        every { containers.getContainer(any(), any(), any()) } answers {
            live[ContainerCoordinates(firstArg(), secondArg(), thirdArg())]
        }
        every { containers.addContainer(any(), any(), any(), any()) } answers {
            live[ContainerCoordinates(firstArg(), secondArg(), thirdArg())] = arg(3)
        }
        every { world.height } returns 2
        val air = Block.None(mockk(relaxed = true))
        every { world.getForeMap(any(), any()) } returns air
        every { world.getBackMap(any(), any()) } returns air
        every { world.getBackMap(33, 1) } returns chestBlock
        every { factory.createContainer(chestBlock, any()) } returns chest
        every { saves.loadChunkEntities(any(), any(), any(), any(), any()) } returns null
        load()
        streamer.onChunkBodiesReady(2)
        streamer.onChunkBodiesReady(2)
        assertEquals(chest, live[coordinate])
        verify(exactly = 1) { factory.createContainer(chestBlock, any()) }

        streamer.onChunkUnloaded(2)
        live.clear()
        every { saves.loadChunkEntities(any(), any(), any(), any(), any()) } returns
            ChunkEntitiesSaveData(emptyList(), emptyList(), mapOf(coordinate to chest))
        load()
        streamer.onChunkBodiesReady(2)
        assertEquals(chest, live[coordinate])
        verify(exactly = 1) { factory.createContainer(chestBlock, any()) }
    }

    private fun load() {
        resident.add(2)
        streamer.onChunkLoaded(2)
    }

    private fun mob(): Mob = mockk<Mob>(relaxed = true).also {
        every { it.position } returns Vector2(33f, 20f)
    }
}
