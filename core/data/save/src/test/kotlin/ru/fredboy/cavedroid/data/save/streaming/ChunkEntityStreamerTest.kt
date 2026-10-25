package ru.fredboy.cavedroid.data.save.streaming

import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
    private val boats = mockk<ru.fredboy.cavedroid.game.controller.vehicle.VehicleController>(relaxed = true)
    private val mobFactory = mockk<ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory>(relaxed = true)
    private val streamer = ChunkEntityStreamer(world, mobController, drops, containers, saves, app, context, mockk(), mockk(), factory, boats, mobFactory)

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
        every { saves.saveChunkEntities(any(), any(), any(), any(), any(), any(), any(), true) } answers {
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
            saves.saveChunkEntities(any(), any(), 2, any(), any(), any(), any(), true)
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

    @ParameterizedTest
    @CsvSource("mineshaft,1", "village,0")
    fun `generated container initializes once and saved empty chest is not refilled`(lootTable: String, layer: Int) {
        val chestBlock = Block.Chest(mockk(relaxed = true), lootTable)
        val chest = mockk<Container>()
        val coordinate = ContainerCoordinates(33, 1, layer)
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
        if (layer == 0) {
            every { world.getForeMap(33, 1) } returns chestBlock
        } else {
            every { world.getBackMap(33, 1) } returns chestBlock
        }
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

    @Test
    fun `boats restore once and are saved with their owning chunk before eviction`() {
        val state = ru.fredboy.cavedroid.entity.vehicle.VehicleState("boat", x = 33f, y = 10f)
        val boat = mockk<ru.fredboy.cavedroid.entity.vehicle.Vehicle>()
        every { boat.position } returns Vector2(state.x, state.y)
        every { boat.snapshot() } returns state
        every { boats.vehicles } returns listOf(boat)
        every { saves.loadChunkEntities(any(), any(), any(), any(), any()) } returns
            ChunkEntitiesSaveData(emptyList(), emptyList(), emptyMap(), listOf(state))
        load()
        streamer.onChunkBodiesReady(2)
        streamer.onChunkBodiesReady(2)
        streamer.onChunkUnloaded(2)
        verify(exactly = 1) { boats.restore(listOf(state)) }
        verifyOrder {
            saves.saveChunkEntities(any(), any(), 2, any(), any(), any(), listOf(state), true)
            boats.evict(boat)
        }
    }

    @Test
    fun `saved passenger is resolved only after the distant player chunk is restored`() {
        val state = ru.fredboy.cavedroid.entity.vehicle.VehicleState("far-boat", x = 504f, y = 20f)
        every { mobController.player.position } returns Vector2(state.x, state.y)
        every { saves.loadChunkEntities(any(), any(), 31, any(), any()) } returns
            ChunkEntitiesSaveData(emptyList(), emptyList(), emptyMap(), listOf(state))

        streamer.onChunkLoaded(0)
        streamer.onChunkBodiesReady(0)
        streamer.onChunkLoaded(31)
        verify(exactly = 0) { boats.resolvePassenger() }

        streamer.onChunkBodiesReady(31)
        streamer.onChunkBodiesReady(31)
        verifyOrder {
            boats.restore(listOf(state))
            boats.resolvePassenger()
        }
        verify(exactly = 1) { boats.resolvePassenger() }
    }

    @Test
    fun `missing passenger is resolved once the player chunk is ready even without a snapshot`() {
        every { mobController.player.position } returns Vector2(-0.5f, 20f)
        every { saves.loadChunkEntities(any(), any(), -1, any(), any()) } returns null
        streamer.onChunkLoaded(-1)
        verify(exactly = 0) { boats.resolvePassenger() }
        streamer.onChunkBodiesReady(-1)
        streamer.onChunkBodiesReady(-1)
        verify(exactly = 1) { boats.resolvePassenger() }
    }

    @Test
    fun `new finite worlds do not restore vehicles from a previous directory`() {
        every { world.isInfinite } returns false
        every { context.isLoadGame() } returns false
        ChunkEntityStreamer(world, mobController, drops, containers, saves, app, context, mockk(), mockk(), factory, boats, mobFactory).initialize()
        verify(exactly = 0) { saves.loadVehicles(any(), any()) }
    }

    @Test
    fun `village beds get one resident and a saved population marker prevents replacement`() {
        every { world.villageBeds(32, 16) } returns listOf(34 to 20)
        every { world.getForeMap(34, 20).params.key } returns "bed_l"
        every { world.getForeMap(35, 20).params.key } returns "bed_r"
        val villager = mockk<ru.fredboy.cavedroid.entity.mob.model.VillagerMob>(relaxed = true)
        every { mobFactory.create(35f, 19.99f, "villager") } returns villager
        every { saves.loadChunkEntities(any(), any(), any(), any(), any()) } returns null
        load()
        streamer.onChunkBodiesReady(2)
        streamer.onChunkBodiesReady(2)
        verify(exactly = 1) { mobFactory.create(35f, 19.99f, "villager") }
        verify { villager.home = ru.fredboy.cavedroid.entity.mob.model.VillagerMob.BlockPosition(34, 20) }
        streamer.onChunkUnloaded(2)
        verify { saves.saveChunkEntities(any(), any(), 2, any(), any(), any(), any(), true) }
        every { saves.loadChunkEntities(any(), any(), any(), any(), any()) } returns
            ChunkEntitiesSaveData(emptyList(), emptyList(), emptyMap(), villageResidentsInitialized = true)
        load()
        streamer.onChunkBodiesReady(2)
        verify(exactly = 1) { mobFactory.create(any(), any(), "villager") }
    }

    @Test
    fun `a destroyed original village bed does not get a resident`() {
        every { world.villageBeds(32, 16) } returns listOf(34 to 20)
        every { world.getForeMap(34, 20).params.key } returns "none"
        load()
        streamer.onChunkBodiesReady(2)
        verify(exactly = 0) { mobFactory.create(any(), any(), "villager") }
    }

    private fun load() {
        resident.add(2)
        streamer.onChunkLoaded(2)
    }

    private fun mob(): Mob = mockk<Mob>(relaxed = true).also {
        every { it.position } returns Vector2(33f, 20f)
    }
}
