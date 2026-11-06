package ru.fredboy.cavedroid.data.save.repository

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.utils.DateFormatter
import ru.fredboy.cavedroid.data.save.mapper.ChestMapper
import ru.fredboy.cavedroid.data.save.mapper.ContainerControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.ControlModeMapper
import ru.fredboy.cavedroid.data.save.mapper.DirectionMapper
import ru.fredboy.cavedroid.data.save.mapper.DropControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.DropMapper
import ru.fredboy.cavedroid.data.save.mapper.FireControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.GrowBlocksMapper
import ru.fredboy.cavedroid.data.save.mapper.InventoryItemMapper
import ru.fredboy.cavedroid.data.save.mapper.InventoryMapper
import ru.fredboy.cavedroid.data.save.mapper.MobControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.PlayerMapper
import ru.fredboy.cavedroid.data.save.mapper.ProjectileControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.ProjectileMapper
import ru.fredboy.cavedroid.data.save.mapper.TntMapper
import ru.fredboy.cavedroid.data.save.mapper.WalkingMobMapper
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.save.PreparedSave
import ru.fredboy.cavedroid.domain.save.model.FireEntry
import ru.fredboy.cavedroid.domain.save.model.GrowBlockEntry
import ru.fredboy.cavedroid.domain.save.model.TntEntry
import ru.fredboy.cavedroid.domain.save.persist
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob
import ru.fredboy.cavedroid.entity.mob.model.WearingArmor
import ru.fredboy.cavedroid.entity.projectile.model.Projectile
import ru.fredboy.cavedroid.entity.vehicle.VehicleState
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
class PreparedSaveTest {
    @TempDir
    lateinit var directory: File
    private var previousFiles: Files? = null
    private var previousGraphics: Graphics? = null
    private lateinit var repository: SaveDataRepositoryImpl
    private val empty = Item.None(mockk { every { key } returns "none" })
    private val diamond = Item.Normal(mockk { every { key } returns "diamond" }, mockk())
    private val stone = mockk<Block> { every { params.key } returns "stone" }
    private val air = mockk<Block> { every { params.key } returns "none" }
    private val items = mockk<ItemsRepository> {
        every { fallbackItem } returns empty
        every { getItemByKey("diamond") } returns diamond
        every { getItemByKey("none") } returns empty
        every { getBlockByKey("stone") } returns stone
        every { getBlockByKey("none") } returns air
    }
    private val getItem = GetItemByKeyUseCase(items)
    private val fallback = GetFallbackItemUseCase(items)
    private val inventoryItemMapper = InventoryItemMapper(getItem)
    private val inventoryMapper = InventoryMapper(inventoryItemMapper, fallback)
    private val mobMapper = MobControllerMapper(
        WalkingMobMapper(DirectionMapper(), mockk(), getItem),
        mockk(),
        PlayerMapper(DirectionMapper(), inventoryMapper, ControlModeMapper(), fallback, mockk(), mockk(), mockk(), mockk()),
        fallback, mockk(), getItem, mockk(), mockk(), mockk(), mockk(), mockk(),
    )
    private val chest = Chest(empty)
    private val chestCoordinates = ContainerCoordinates(1, 2, 1)

    @BeforeEach
    fun setUp() {
        previousFiles = Gdx.files
        previousGraphics = Gdx.graphics
        Gdx.files = mockk<Files> {
            every { getFileHandle(any(), Files.FileType.Absolute) } answers { FileHandle(File(firstArg<String>())) }
        }
        Gdx.graphics = mockk<Graphics> {
            every { width } returns 200
            every { height } returns 200
        }
        repository = SaveDataRepositoryImpl(
            itemsRepository = items,
            dropControllerMapper = DropControllerMapper(DropMapper(inventoryItemMapper), getItem, mockk(), mockk()),
            containerControllerMapper = ContainerControllerMapper(ChestMapper(inventoryItemMapper, fallback), mockk(), mockk(), getItem),
            mobControllerMapper = mobMapper,
            gameSaveInfoMapper = mockk(),
            projectileControllerMapper = ProjectileControllerMapper(ProjectileMapper(getItem), getItem),
            growBlocksMapper = GrowBlocksMapper(),
            fireControllerMapper = FireControllerMapper(),
            tntMapper = TntMapper(),
            getItemByKeyUseCase = getItem,
            applicationContextRepository = mockk<ApplicationContextRepository> { every { getGameDirectoryFileType() } returns Files.FileType.Absolute },
            dateFormatter = DateFormatter(),
        )
        chest.items[0] = diamond.toInventoryItem(7)
    }

    @AfterEach
    fun tearDown() {
        Gdx.files = previousFiles
        Gdx.graphics = previousGraphics
    }

    @Test
    fun `finite save persists capture time terrain inventories mobs projectiles timers vehicles and metadata`() {
        val fore = arrayOf(arrayOf(stone))
        val back = arrayOf(arrayOf(air))
        val biomes = arrayOf(Biome.PLAINS)
        val world = mockk<GameWorld>(relaxed = true) {
            every { isInfinite } returns false
            every { foreMap } returns fore
            every { backMap } returns back
            every { this@mockk.biomes } returns biomes
            every { generatorConfig } returns WorldGeneratorConfig.getDefault()
            every { currentGameTime } returns 42f
        }
        val inventory = Inventory(1, empty).apply { items[0] = diamond.toInventoryItem(3) }
        val playerPosition = Vector2(4f, 5f)
        val player = mockk<Player>(relaxed = true) {
            every { params.key } returns "char"
            every { position } returns playerPosition
            every { direction } returns Direction.RIGHT
            every { controlMode } returns Player.ControlMode.WALK
            every { gameMode } returns GameMode.SURVIVAL
            every { this@mockk.inventory } returns inventory
            every { wearingArmor } returns WearingArmor(empty)
        }
        val mobPosition = Vector2(10f, 11f)
        val mob = mockk<WalkingMob>(relaxed = true) {
            every { params.key } returns "pig"
            every { position } returns mobPosition
            every { direction } returns Direction.LEFT
        }
        val mobs = mockk<MobController> {
            every { this@mockk.player } returns player
            every { this@mockk.mobs } returns listOf(mob)
            every { villageResidentsInitialized } returns true
        }
        val containers = mockk<ContainerController> { every { containerMap } returns mutableMapOf(chestCoordinates to chest) }
        val projectilePosition = Vector2(12f, 13f)
        val projectile = mockk<Projectile>(relaxed = true) {
            every { position } returns projectilePosition
            every { item } returns diamond
        }
        val projectiles = mockk<ProjectileController> { every { this@mockk.projectiles } returns mutableListOf(projectile) }
        val growth = mutableListOf(GrowBlockEntry(1, 2, "sapling", 30))
        val fire = mutableListOf(FireEntry(1, 2, Layer.FOREGROUND, 4f))
        val vehicles = mutableListOf(VehicleState("boat", x = 7f, y = 8f))
        val tnt = mutableListOf(TntEntry(1f, 2f, 3f, 4f, 5f))
        val prepared = repository.prepareSave(directory.path, "world", "Captured", mockk(relaxed = true), mobs, containers, world, projectiles, growth, fire, tnt, vehicles)
        fore[0][0] = air
        back[0][0] = stone
        biomes[0] = Biome.DESERT
        inventory.clear()
        chest.items[0] = empty.toInventoryItem()
        playerPosition.setZero()
        mobPosition.setZero()
        projectilePosition.setZero()
        growth.clear()
        fire.clear()
        vehicles.clear()
        tnt.clear()
        every { world.currentGameTime } returns 99f
        prepared.persist()
        val loaded = repository.loadMap(directory.path, "world")
        assertSame(stone, loaded.foreMap!![0][0])
        assertSame(air, loaded.backMap!![0][0])
        assertEquals(Biome.PLAINS, loaded.biomes!![0])
        assertEquals(42f, loaded.gameTime)
        val savedMobs = decode<SaveDataDto.MobControllerSaveDataDto>("mobs.dat")
        assertEquals(4f, savedMobs.player.x)
        assertEquals(3, savedMobs.player.inventory.items.single().amount)
        assertEquals(10f, savedMobs.mobs.single().x)
        val savedContainers = decode<SaveDataDto.ContainerControllerSaveDataDto>("containers.dat")
        assertEquals(7, savedContainers.containerMap.values.single().items.first().amount)
        assertEquals(12f, decode<SaveDataDto.ProjectileControllerSaveDataDto>("projectiles.dat").projectiles.single().x)
        assertEquals(30L, repository.loadGrowBlockEntries(directory.path, "world").single().remainingTicks)
        assertEquals(4f, repository.loadFireEntries(directory.path, "world").single().age)
        assertEquals(7f, repository.loadVehicles(directory.path, "world").single().x)
        assertEquals(5f, repository.loadTntEntries(directory.path, "world").single().fuse)
        assertEquals("Captured", repository.getSaveDetails(directory.path, "world").name)
        assertFalse(File(directory, "saves/world/screenshot.png").exists())
    }

    @Test
    fun `chunk terrain copies input arrays before encoding`() {
        val fore = arrayOf(arrayOf(stone))
        val back = arrayOf(arrayOf(air))
        val biomes = arrayOf(Biome.PLAINS)
        val prepared = repository.prepareSaveInfiniteChunk(directory.path, "world", -1, fore, back, biomes)
        fore[0][0] = air
        back[0][0] = stone
        biomes[0] = Biome.DESERT
        prepared.persist()
        val loaded = requireNotNull(repository.loadInfiniteChunk(directory.path, "world", -1))
        assertSame(stone, loaded.foreMap[0][0])
        assertSame(air, loaded.backMap[0][0])
        assertEquals(Biome.PLAINS, loaded.biomes[0])
    }

    @Test
    fun `newer streamed writes and deletions survive delayed autosave for every chunk payload`() {
        for (kind in listOf("entities", "fire", "growth")) {
            val old = prepareChunk(kind, 1)
            prepareChunk(kind, 2).persist()
            old.persist()
            assertChunk(kind, 2)
            val stale = prepareChunk(kind, 1)
            prepareChunk(kind, 0).persist()
            stale.persist()
            assertChunk(kind, 0)
        }
    }

    @Test
    fun `streamed chest and village marker are independent after capture`() {
        val prepared = repository.prepareSaveChunkEntities(directory.path, "world", 0, emptyList(), emptyList(), mapOf(chestCoordinates to chest), villageResidentsInitialized = true)
        chest.items[0] = empty.toInventoryItem()
        prepared.persist()
        val loaded = requireNotNull(repository.loadChunkEntities(directory.path, "world", 0, mockk(), mockk()))
        assertTrue(loaded.villageResidentsInitialized)
        assertEquals(7, loaded.containers.getValue(chestCoordinates).items.first().amount)
    }

    private fun prepareChunk(kind: String, value: Int): PreparedSave = when (kind) {
        "entities" -> repository.prepareSaveChunkEntities(directory.path, "world", 0, emptyList(), emptyList(), emptyMap(), if (value == 0) emptyList() else listOf(VehicleState("boat", x = value.toFloat(), y = 0f)))
        "fire" -> repository.prepareSaveChunkFire(directory.path, "world", 0, if (value == 0) emptyList() else listOf(FireEntry(value, 1, Layer.FOREGROUND, 1f)))
        else -> repository.prepareSaveChunkGrowBlocks(directory.path, "world", 0, if (value == 0) emptyList() else listOf(GrowBlockEntry(value, 1, "sapling", 1)))
    }

    private fun assertChunk(kind: String, value: Int) {
        when (kind) {
            "entities" -> assertEquals(if (value == 0) null else value.toFloat(), repository.loadChunkEntities(directory.path, "world", 0, mockk(), mockk())?.vehicles?.single()?.x)
            "fire" -> assertEquals(if (value == 0) null else value, repository.loadChunkFire(directory.path, "world", 0).singleOrNull()?.x)
            else -> assertEquals(if (value == 0) null else value, repository.loadChunkGrowBlocks(directory.path, "world", 0).singleOrNull()?.x)
        }
    }

    private inline fun <reified T> decode(name: String): T = ProtoBuf.decodeFromByteArray(File(directory, "saves/world/$name").readBytes())
}
