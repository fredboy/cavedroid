package ru.fredboy.cavedroid.data.save.repository

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.utils.TimeUtils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import ru.fredboy.cavedroid.common.CaveDroidConstants.MAX_SAVES_COUNT
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.model.WorldType
import ru.fredboy.cavedroid.common.utils.DateFormatter
import ru.fredboy.cavedroid.data.save.mapper.ContainerControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.DropControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.FireControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.GameSaveInfoMapper
import ru.fredboy.cavedroid.data.save.mapper.GrowBlocksMapper
import ru.fredboy.cavedroid.data.save.mapper.MobControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.ProjectileControllerMapper
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.save.model.ChunkSaveData
import ru.fredboy.cavedroid.domain.save.model.FireEntry
import ru.fredboy.cavedroid.domain.save.model.GameMapSaveData
import ru.fredboy.cavedroid.domain.save.model.GameSaveDetails
import ru.fredboy.cavedroid.domain.save.model.GameSaveInfo
import ru.fredboy.cavedroid.domain.save.model.GrowBlockEntry
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Weather
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.MobQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.projectile.abstraction.ProjectileWorldAdapter
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.MobSoundManager
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.world.GameWorld
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject

@OptIn(ExperimentalSerializationApi::class)
internal class SaveDataRepositoryImpl @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val dropControllerMapper: DropControllerMapper,
    private val containerControllerMapper: ContainerControllerMapper,
    private val mobControllerMapper: MobControllerMapper,
    private val gameSaveInfoMapper: GameSaveInfoMapper,
    private val projectileControllerMapper: ProjectileControllerMapper,
    private val growBlocksMapper: GrowBlocksMapper,
    private val fireControllerMapper: FireControllerMapper,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val applicationContextRepository: ApplicationContextRepository,
    private val dateFormatter: DateFormatter,
) : SaveDataRepository {

    private val fileType: Files.FileType
        get() = applicationContextRepository.getGameDirectoryFileType()

    private fun file(path: String): FileHandle {
        // Absolute paths must keep the leading '/' so libGDX wraps them in an
        // absolute File. Local/Internal paths are relative to the platform's
        // base dir and the leading slash would either be rejected (web) or
        // mis-resolved, so trim it for those.
        val resolved = if (fileType == Files.FileType.Absolute) path else path.trimStart('/')
        return Gdx.files.getFileHandle(resolved, fileType)
    }

    private fun Int.toByteArray(): ByteArray = ByteBuffer.allocate(Int.SIZE_BYTES)
        .putInt(this)
        .array()

    private fun Short.toByteArray(): ByteArray = ByteBuffer.allocate(Short.SIZE_BYTES)
        .putShort(this)
        .array()

    private fun buildBlocksDictionary(
        foreMap: Array<Array<Block>>,
        backMap: Array<Array<Block>>,
    ): Map<String, Int> {
        val maps = sequenceOf(foreMap.asSequence(), backMap.asSequence())

        return maps.flatten()
            .flatMap(Array<Block>::asSequence)
            .map { it.params.key }
            .toSet()
            .mapIndexed { index, key -> key to index }
            .toMap()
    }

    private fun saveDict(file: FileHandle, dict: Map<String, Int>) {
        val result = dict.asSequence()
            .sortedBy { it.value }
            .joinToString(separator = "\n") { it.key }
            .encodeToByteArray()

        file.writeBytes(result, false)
    }

    private fun compressMap(map: Array<Array<Block>>, dict: Map<String, Int>): ByteArray {
        if (dict.size > 0xff) {
            throw IllegalArgumentException("Cannot save this map as bytes")
        }

        val width = map.size
        val height = map[0].size

        val blocks = sequence {
            for (y in 0..<height) {
                for (x in 0..<width) {
                    yield(map[x][y])
                }
            }
        }

        val result = sequence {
            var run = 0
            var runValue: UByte? = null

            yield(MAP_SAVE_VERSION.toByte())
            width.toByteArray().forEach { yield(it) }
            height.toByteArray().forEach { yield(it) }

            blocks.forEach { block ->
                val key = block.params.key

                val blockId = dict[key]?.toUByte()
                    ?: throw IllegalArgumentException("Dictionary does not contain key $key")

                if (blockId != runValue || run == Int.MAX_VALUE) {
                    if (run > 0 && runValue != null) {
                        run.toByteArray().forEach { yield(it) }
                        yield(runValue!!.toByte())
                    }
                    run = 1
                    runValue = blockId
                } else {
                    run++
                }
            }

            run.toByteArray().forEach { yield(it) }
            yield(runValue!!.toByte())
        }

        return result.toList().toByteArray()
    }

    private fun decompressMap(
        bytes: ByteArray,
        dict: List<String>,
        itemsRepository: ItemsRepository,
    ): Array<Array<Block>> {
        val version = bytes.first().toUByte()
        require(version == MAP_SAVE_VERSION)

        val width = ByteBuffer.wrap(bytes, 1, Int.SIZE_BYTES).getInt()
        val height = ByteBuffer.wrap(bytes, 1 + Int.SIZE_BYTES, Int.SIZE_BYTES).getInt()

        val blocks = buildList {
            for (i in 1 + (Int.SIZE_BYTES shl 1)..bytes.lastIndex step Int.SIZE_BYTES + 1) {
                val run = ByteBuffer.wrap(bytes, i, Int.SIZE_BYTES).getInt()
                val blockId = bytes[i + Int.SIZE_BYTES].toUByte().toInt()

                for (j in 0..<run) {
                    add(itemsRepository.getBlockByKey(dict[blockId]))
                }
            }
        }

        return Array(width) { x ->
            Array(height) { y ->
                blocks[x + y * width]
            }
        }
    }

    private fun compressBiomes(biomes: Array<Biome>): ByteArray {
        val width = biomes.size

        val result = sequence {
            var run = 0
            var runValue: UByte? = null

            yield(MAP_SAVE_VERSION.toByte())
            width.toByteArray().forEach { yield(it) }

            for (x in 0..<width) {
                val ordinal = biomes[x].ordinal.toUByte()

                if (ordinal != runValue || run == Int.MAX_VALUE) {
                    if (run > 0 && runValue != null) {
                        run.toByteArray().forEach { yield(it) }
                        yield(runValue!!.toByte())
                    }
                    run = 1
                    runValue = ordinal
                } else {
                    run++
                }
            }

            run.toByteArray().forEach { yield(it) }
            yield(runValue!!.toByte())
        }

        return result.toList().toByteArray()
    }

    private fun decompressBiomes(bytes: ByteArray): Array<Biome> {
        val version = bytes.first().toUByte()
        require(version == MAP_SAVE_VERSION)

        val width = ByteBuffer.wrap(bytes, 1, Int.SIZE_BYTES).getInt()
        val biomeEntries = Biome.entries

        val list = buildList {
            for (i in 1 + Int.SIZE_BYTES..bytes.lastIndex step Int.SIZE_BYTES + 1) {
                val run = ByteBuffer.wrap(bytes, i, Int.SIZE_BYTES).getInt()
                val ordinal = bytes[i + Int.SIZE_BYTES].toUByte().toInt()
                val biome = biomeEntries[ordinal]
                repeat(run) { add(biome) }
            }
        }

        return Array(width) { list[it] }
    }

    private fun internalLoadMap(
        savesPath: String,
    ): GameMapSaveData {
        val meta = loadMapData(savesPath)
        val worldType = if (meta.worldType == WorldType.INFINITE.ordinal) WorldType.INFINITE else WorldType.LOOPING

        if (worldType == WorldType.INFINITE) {
            // Infinite worlds keep no whole-map files; terrain lives in per-chunk files and is
            // regenerated from the seed. Return metadata only.
            return GameMapSaveData(
                foreMap = null,
                backMap = null,
                biomes = null,
                gameTime = meta.gameTime,
                moonPhase = meta.moonPhase,
                totalGameTime = meta.totalGameTime ?: meta.gameTime,
                lastSpawnGameTime = meta.lastSpawnGameTime ?: 0f,
                weather = meta.weather?.let { Weather.entries.getOrNull(it) },
                weatherTimer = meta.weatherTimer,
                weatherIntensity = meta.weatherIntensity,
                currentStreakStartDayIndex = meta.currentStreakStartDayIndex ?: 0,
                worldType = WorldType.INFINITE,
                seed = meta.seed,
            )
        }

        val dict = file("$savesPath/$DICT_FILE").readString().split("\n")

        val foreMap: Array<Array<Block>>
        with(GZIPInputStream(file("$savesPath/$FOREMAP_FILE").read())) {
            foreMap = decompressMap(readBytes(), dict, itemsRepository)
            close()
        }

        val backMap: Array<Array<Block>>
        with(GZIPInputStream(file("$savesPath/$BACKMAP_FILE").read())) {
            backMap = decompressMap(readBytes(), dict, itemsRepository)
            close()
        }

        val biomesFile = file("$savesPath/$BIOMES_FILE")
        val biomes: Array<Biome>? = if (biomesFile.exists()) {
            GZIPInputStream(biomesFile.read()).use { decompressBiomes(it.readBytes()) }
        } else {
            null
        }

        return GameMapSaveData(
            foreMap = foreMap,
            backMap = backMap,
            biomes = biomes,
            gameTime = meta.gameTime,
            moonPhase = meta.moonPhase,
            totalGameTime = meta.totalGameTime ?: meta.gameTime,
            lastSpawnGameTime = meta.lastSpawnGameTime ?: 0f,
            weather = meta.weather?.let { Weather.entries.getOrNull(it) },
            weatherTimer = meta.weatherTimer,
            weatherIntensity = meta.weatherIntensity,
            currentStreakStartDayIndex = meta.currentStreakStartDayIndex ?: 0,
            worldType = WorldType.LOOPING,
            seed = meta.seed,
        )
    }

    private fun saveMap(gameWorld: GameWorld, savesPath: String) {
        val fullForeMap = gameWorld.foreMap
        val fullBackMap = gameWorld.backMap

        val dict = buildBlocksDictionary(fullForeMap, fullBackMap)

        saveDict(file("$savesPath/$DICT_FILE"), dict)

        with(GZIPOutputStream(file("$savesPath/$FOREMAP_FILE").write(false))) {
            write(compressMap(fullForeMap, dict))
            close()
        }

        with(GZIPOutputStream(file("$savesPath/$BACKMAP_FILE").write(false))) {
            write(compressMap(fullBackMap, dict))
            close()
        }

        GZIPOutputStream(file("$savesPath/$BIOMES_FILE").write(false)).use {
            it.write(compressBiomes(gameWorld.biomes))
        }
    }

    private fun saveMapData(gameWorld: GameWorld, savesPath: String, worldName: String, gameMode: GameMode) {
        val metaFile = file("$savesPath/$META_FILE")

        val now = TimeUtils.millis()
        // On the first save (no existing meta) the world was just generated, so
        // its generatorConfig holds the real seed; on re-saves the original seed
        // and creation time are preserved (a loaded world's generatorConfig seed
        // is a fresh meaningless value).
        val existing = metaFile.takeIf { it.exists() }
            ?.let { runCatching { loadMapData(savesPath) }.getOrNull() }
        val createdTimestamp = existing?.let { it.createdTimestamp ?: it.timestamp } ?: now
        val seed = if (existing != null) existing.seed else gameWorld.generatorConfig.seed

        val worldSaveDataDto = SaveDataDto.WorldSaveDataDto(
            version = MAP_SAVE_VERSION.toInt(),
            name = worldName,
            timestamp = now,
            gameTime = gameWorld.currentGameTime,
            moonPhase = gameWorld.moonPhase,
            gameMode = gameMode,
            totalGameTime = gameWorld.totalGameTimeSec,
            lastSpawnGameTime = gameWorld.lastSpawnGameTime,
            weather = gameWorld.weather.ordinal,
            weatherTimer = gameWorld.weatherTimer,
            weatherIntensity = gameWorld.weatherIntensity,
            currentStreakStartDayIndex = gameWorld.currentStreakStartDayIndex,
            createdTimestamp = createdTimestamp,
            seed = seed,
            worldType = if (gameWorld.isInfinite) WorldType.INFINITE.ordinal else WorldType.LOOPING.ordinal,
        )

        val bytes = ProtoBuf.encodeToByteArray(worldSaveDataDto)

        metaFile.writeBytes(bytes, false)
    }

    private fun chunkPath(savesPath: String, chunkX: Int): String = "$savesPath/$CHUNKS_DIR/$chunkX"

    override fun saveInfiniteChunk(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
        foreMap: Array<Array<Block>>,
        backMap: Array<Array<Block>>,
        biomes: Array<Biome>,
    ) {
        val path = chunkPath(getSavePath(gameDataFolder, saveGameDirectory), chunkX)
        val dict = buildBlocksDictionary(foreMap, backMap)

        saveDict(file("$path/$DICT_FILE"), dict)
        GZIPOutputStream(file("$path/$FOREMAP_FILE").write(false)).use { it.write(compressMap(foreMap, dict)) }
        GZIPOutputStream(file("$path/$BACKMAP_FILE").write(false)).use { it.write(compressMap(backMap, dict)) }
        GZIPOutputStream(file("$path/$BIOMES_FILE").write(false)).use { it.write(compressBiomes(biomes)) }
    }

    override fun loadInfiniteChunk(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
    ): ChunkSaveData? {
        val path = chunkPath(getSavePath(gameDataFolder, saveGameDirectory), chunkX)
        val dictFile = file("$path/$DICT_FILE")
        if (!dictFile.exists()) {
            return null
        }

        val dict = dictFile.readString().split("\n")
        val foreMap = GZIPInputStream(file("$path/$FOREMAP_FILE").read())
            .use { decompressMap(it.readBytes(), dict, itemsRepository) }
        val backMap = GZIPInputStream(file("$path/$BACKMAP_FILE").read())
            .use { decompressMap(it.readBytes(), dict, itemsRepository) }
        val biomes = GZIPInputStream(file("$path/$BIOMES_FILE").read())
            .use { decompressBiomes(it.readBytes()) }

        return ChunkSaveData(foreMap = foreMap, backMap = backMap, biomes = biomes)
    }

    private fun loadMapData(savesPath: String): SaveDataDto.WorldSaveDataDto {
        val metaFile = file("$savesPath/$META_FILE")

        val bytes = metaFile.readBytes()

        return ProtoBuf.decodeFromByteArray<SaveDataDto.WorldSaveDataDto>(bytes)
    }

    private fun takeScreenshot(savesPath: String) {
        val screenshotHalfSize = 128
        val halfWidth = Gdx.graphics.width / 2
        val halfHeight = Gdx.graphics.height / 2

        if (halfWidth < screenshotHalfSize || halfHeight < screenshotHalfSize) {
            return
        }

        val pixmap = Pixmap.createFromFrameBuffer(
            halfWidth - screenshotHalfSize,
            halfHeight - screenshotHalfSize,
            screenshotHalfSize shl 1,
            screenshotHalfSize shl 1,
        )

        val size = screenshotHalfSize * screenshotHalfSize * 16
        for (i in 3 until size step 4) {
            pixmap.pixels.put(i, 255.toByte())
        }

        PixmapIO.writePNG(file("$savesPath/$SCREENSHOT_FILE"), pixmap, Deflater.DEFAULT_COMPRESSION, true)
        pixmap.dispose()
    }

    override fun getActualSaveDirName(
        gameDataFolder: String,
        saveGameDirectory: String,
        overwrite: Boolean,
    ): String {
        if (overwrite) {
            return saveGameDirectory
        }

        var savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        var saveDirHandle = file(savesPath)
        var suffix = 0

        while (saveDirHandle.exists() && suffix < 256) {
            savesPath = getSavePath(gameDataFolder, "${saveGameDirectory}_${++suffix}")
            saveDirHandle = file(savesPath)
        }

        return saveDirHandle.name()
    }

    override fun save(
        gameDataFolder: String,
        saveGameDirectory: String,
        worldName: String,
        dropController: DropController,
        mobController: MobController,
        containerController: ContainerController,
        gameWorld: GameWorld,
        projectileController: ProjectileController,
        growBlockEntries: List<GrowBlockEntry>,
        fireEntries: List<FireEntry>,
    ) {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)

        val dropFile = file("$savesPath/$DROP_FILE")
        val mobsFile = file("$savesPath/$MOBS_FILE")
        val containersFile = file("$savesPath/$CONTAINERS_FILE")
        val projectilesFile = file("$savesPath/$PROJECTILES_FILE")
        val growBlocksFile = file("$savesPath/$GROW_BLOCKS_FILE")
        val fireFile = file("$savesPath/$FIRE_FILE")

        val dropBytes = ProtoBuf.encodeToByteArray(dropControllerMapper.mapSaveData(dropController))
        val mobsBytes = ProtoBuf.encodeToByteArray(mobControllerMapper.mapSaveData(mobController))
        val containersBytes =
            ProtoBuf.encodeToByteArray(containerControllerMapper.mapSaveData(containerController))
        val projectilesBytes = ProtoBuf.encodeToByteArray(projectileControllerMapper.mapSaveData(projectileController))
        val growBlocksBytes = ProtoBuf.encodeToByteArray(growBlocksMapper.mapSaveData(growBlockEntries))
        val fireBytes = ProtoBuf.encodeToByteArray(fireControllerMapper.mapSaveData(fireEntries))

        dropFile.writeBytes(dropBytes, false)
        mobsFile.writeBytes(mobsBytes, false)
        containersFile.writeBytes(containersBytes, false)
        projectilesFile.writeBytes(projectilesBytes, false)
        growBlocksFile.writeBytes(growBlocksBytes, false)
        GZIPOutputStream(fireFile.write(false)).use { it.write(fireBytes) }

        if (gameWorld.isInfinite) {
            // Terrain is persisted per chunk; flush whatever is resident and dirty.
            gameWorld.flushDirtyChunks()
        } else {
            saveMap(gameWorld, savesPath)
        }

        saveMapData(gameWorld, savesPath, worldName, mobController.player.gameMode)

        takeScreenshot(savesPath)
    }

    override fun loadGrowBlockEntries(
        gameDataFolder: String,
        saveGameDirectory: String,
    ): List<GrowBlockEntry> {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        val growBlocksFile = file("$savesPath/$GROW_BLOCKS_FILE")

        if (!growBlocksFile.exists()) {
            return emptyList()
        }

        return ProtoBuf.decodeFromByteArray<SaveDataDto.GrowBlocksSaveDataDto>(growBlocksFile.readBytes())
            .let(growBlocksMapper::mapEntries)
    }

    override fun loadFireEntries(
        gameDataFolder: String,
        saveGameDirectory: String,
    ): List<FireEntry> {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        val fireFile = file("$savesPath/$FIRE_FILE")

        if (!fireFile.exists()) {
            return emptyList()
        }

        val bytes = GZIPInputStream(fireFile.read()).use { it.readBytes() }
        return ProtoBuf.decodeFromByteArray<SaveDataDto.FireControllerSaveDataDto>(bytes)
            .let(fireControllerMapper::mapEntries)
    }

    override fun loadMap(
        gameDataFolder: String,
        saveGameDirectory: String,
    ): GameMapSaveData {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        return internalLoadMap(savesPath)
    }

    override fun loadContainerController(
        gameDataFolder: String,
        saveGameDirectory: String,
        containerWorldAdapter: ContainerWorldAdapter,
        containerFactory: ContainerFactory,
        dropAdapter: DropAdapter,
    ): ContainerController {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        val containersFile = file("$savesPath/$CONTAINERS_FILE")
        val containersBytes = containersFile.readBytes()

        return ProtoBuf.decodeFromByteArray<SaveDataDto.ContainerControllerSaveDataDto>(
            containersBytes,
        ).let { saveData ->
            containerControllerMapper.mapContainerController(
                saveDataDto = saveData,
                containerWorldAdapter = containerWorldAdapter,
                containerFactory = containerFactory,
                dropAdapter = dropAdapter,
            )
        }
    }

    override fun loadDropController(
        gameDataFolder: String,
        saveGameDirectory: String,
        dropWorldAdapter: DropWorldAdapter,
        dropQueue: DropQueue,
        playerAdapter: PlayerAdapter,
    ): DropController {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        val dropFile = file("$savesPath/$DROP_FILE")
        val dropBytes = dropFile.readBytes()

        return ProtoBuf.decodeFromByteArray<SaveDataDto.DropControllerSaveDataDto>(dropBytes)
            .let { saveData ->
                dropControllerMapper.mapDropController(
                    saveDataDto = saveData,
                    dropWorldAdapter = dropWorldAdapter,
                    dropQueue = dropQueue,
                    playerAdapter = playerAdapter,
                )
            }
    }

    override fun loadMobController(
        gameDataFolder: String,
        saveGameDirectory: String,
        mobWorldAdapter: MobWorldAdapter,
        mobPhysicsFactory: MobPhysicsFactory,
        dropQueue: DropQueue,
        mobSoundManager: MobSoundManager,
        projectileAdapter: ProjectileAdapter,
        mobQueue: MobQueue,
    ): MobController {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        val mobsFile = file("$savesPath/$MOBS_FILE")
        val mobsBytes = mobsFile.readBytes()

        return ProtoBuf.decodeFromByteArray<SaveDataDto.MobControllerSaveDataDto>(mobsBytes)
            .let { saveData ->
                mobControllerMapper.mapMobController(
                    saveDataDto = saveData,
                    mobWorldAdapter = mobWorldAdapter,
                    mobPhysicsFactory = mobPhysicsFactory,
                    dropQueue = dropQueue,
                    mobSoundManager = mobSoundManager,
                    projectileAdapter = projectileAdapter,
                    mobQueue = mobQueue,
                )
            }
    }

    override fun loadProjectileController(
        gameDataFolder: String,
        saveGameDirectory: String,
        projectileWorldAdapter: ProjectileWorldAdapter,
        dropQueue: DropQueue,
    ): ProjectileController {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        val projectilesFile = file("$savesPath/$PROJECTILES_FILE")

        if (!projectilesFile.exists()) {
            return ProjectileController(
                getItemByKeyUseCase = getItemByKeyUseCase,
                projectileWorldAdapter = projectileWorldAdapter,
                dropQueue = dropQueue,
            )
        }

        val projectilesBytes = projectilesFile.readBytes()

        return ProtoBuf.decodeFromByteArray<SaveDataDto.ProjectileControllerSaveDataDto>(projectilesBytes)
            .let { saveData ->
                projectileControllerMapper.mapProjectileController(
                    saveDataDto = saveData,
                    projectileWorldAdapter = projectileWorldAdapter,
                    dropQueue = dropQueue,
                )
            }
    }

    private fun getSavePath(
        gameDataFolder: String,
        saveGameDirectory: String,
    ): String {
        return "$gameDataFolder/$SAVES_DIR/$saveGameDirectory"
    }

    private fun isSaveDir(dir: FileHandle): Boolean {
        if (!dir.exists() || !dir.isDirectory) {
            return false
        }

        val files = dir.list()?.map { it.name() }?.toSet() ?: return false

        // Whole-map files (dict/foremap/backmap) only exist for finite worlds; infinite worlds
        // store terrain per chunk, so only the always-present files are required.
        val requiredFiles = setOf(
            DROP_FILE,
            MOBS_FILE,
            CONTAINERS_FILE,
            META_FILE,
        )

        return requiredFiles.all { it in files }
    }

    override fun getSavesInfo(gameDataFolder: String): List<GameSaveInfo> {
        return file("$gameDataFolder/$SAVES_DIR").list()
            .asSequence()
            .filter { it.isDirectory }
            .filter { isSaveDir(it) }
            .mapNotNull { saveDir ->
                // A single corrupt/unreadable meta.dat must not break the whole
                // list — skip it instead of throwing.
                runCatching {
                    val saveData = loadMapData(saveDir.path())
                    gameSaveInfoMapper.map(
                        dto = saveData,
                        dir = saveDir.name(),
                        expectedVersion = MAP_SAVE_VERSION.toInt(),
                        screenshotHandle = saveDir.child(SCREENSHOT_FILE).takeIf { it.exists() },
                    )
                }.onFailure { error ->
                    logger.w(error) { "Skipping unreadable save '${saveDir.name()}'" }
                }.getOrNull()
            }
            .take(MAX_SAVES_COUNT)
            .toList()
            .sortedByDescending { it.lastModifiedTimestamp }
    }

    override fun findCorruptedSaveDirectories(gameDataFolder: String): List<String> {
        return file("$gameDataFolder/$SAVES_DIR").list()
            .asSequence()
            .filter { it.isDirectory }
            .filter { isSaveDir(it) }
            .filter { saveDir -> runCatching { loadMapData(saveDir.path()) }.isFailure }
            .map { it.name() }
            .toList()
    }

    override fun getSaveDetails(gameDataFolder: String, saveDir: String): GameSaveDetails {
        val savesPath = getSavePath(gameDataFolder, saveDir)
        val meta = loadMapData(savesPath)
        val (width, height) = if (meta.worldType == WorldType.INFINITE.ordinal) {
            // Infinite worlds have no fixed dimensions; 0 width signals "infinite" to the UI.
            0 to 0
        } else {
            readMapDimensions(savesPath)
        }

        val sizeBytes = file(savesPath).list()
            .filter { !it.isDirectory }
            .sumOf { it.length() }

        return GameSaveDetails(
            name = meta.name,
            directory = saveDir,
            gameMode = meta.gameMode,
            widthBlocks = width,
            heightBlocks = height,
            sizeBytes = sizeBytes,
            version = meta.version,
            isSupported = meta.version == MAP_SAVE_VERSION.toInt(),
            seed = meta.seed,
            lastModifiedString = dateFormatter.format(meta.timestamp),
            createdString = meta.createdTimestamp?.let(dateFormatter::format),
            screenshotHandle = file("$savesPath/$SCREENSHOT_FILE").takeIf { it.exists() },
        )
    }

    private fun readMapDimensions(savesPath: String): Pair<Int, Int> {
        // The foremap header (written by compressMap) is: version byte, then the
        // width and height as 4-byte ints. Read just that prefix.
        val header = ByteArray(1 + (Int.SIZE_BYTES shl 1))
        GZIPInputStream(file("$savesPath/$FOREMAP_FILE").read()).use { stream ->
            var read = 0
            while (read < header.size) {
                val count = stream.read(header, read, header.size - read)
                if (count < 0) break
                read += count
            }
        }
        val width = ByteBuffer.wrap(header, 1, Int.SIZE_BYTES).getInt()
        val height = ByteBuffer.wrap(header, 1 + Int.SIZE_BYTES, Int.SIZE_BYTES).getInt()
        return width to height
    }

    override fun renameSave(gameDataFolder: String, saveDir: String, newName: String) {
        val savesPath = getSavePath(gameDataFolder, saveDir)
        val meta = loadMapData(savesPath)
        val renamed = meta.copy(name = newName.trim())
        file("$savesPath/$META_FILE").writeBytes(ProtoBuf.encodeToByteArray(renamed), false)
    }

    // A save is exported as a flat, length-prefixed archive rather than a real
    // zip: java.util.zip's ZipOutputStream/ZipInputStream are not faithfully
    // emulated under TeaVM (web) and silently corrupt the bytes. This format
    // relies only on primitives that work on every platform.
    //   magic("CDA1") | entryCount:int | { nameLen:int, name, contentLen:int, content }*
    override fun exportSaveToZip(gameDataFolder: String, saveDir: String): ByteArray {
        val savesPath = getSavePath(gameDataFolder, saveDir)
        val output = ByteArrayOutputStream()

        val files = file(savesPath).list().filter { !it.isDirectory }

        output.write(ARCHIVE_MAGIC)
        output.write(files.size.toByteArray())
        files.forEach { entry ->
            val nameBytes = entry.name().encodeToByteArray()
            val content = entry.readBytes()
            output.write(nameBytes.size.toByteArray())
            output.write(nameBytes)
            output.write(content.size.toByteArray())
            output.write(content)
        }

        return output.toByteArray()
    }

    override fun importSaveFromZip(gameDataFolder: String, zipBytes: ByteArray): String {
        require(getSavesInfo(gameDataFolder).size < MAX_SAVES_COUNT) {
            "Cannot import: maximum number of saves reached"
        }

        val buffer = ByteBuffer.wrap(zipBytes)
        val magic = ByteArray(ARCHIVE_MAGIC.size)
        require(buffer.remaining() >= magic.size) { "Invalid save archive" }
        buffer.get(magic)
        require(magic.contentEquals(ARCHIVE_MAGIC)) { "Invalid save archive" }

        val entryCount = buffer.getInt()
        require(entryCount in 0..MAX_ARCHIVE_ENTRIES) { "Invalid save archive" }

        val newDir = getActualSaveDirName(gameDataFolder, IMPORTED_SAVE_DIR, overwrite = false)
        val savesPath = getSavePath(gameDataFolder, newDir)
        val saveDirHandle = file(savesPath)

        try {
            repeat(entryCount) {
                val nameLen = buffer.getInt()
                require(nameLen in 0..MAX_ARCHIVE_NAME_LENGTH && nameLen <= buffer.remaining()) {
                    "Invalid save archive"
                }
                val nameBytes = ByteArray(nameLen)
                buffer.get(nameBytes)

                val contentLen = buffer.getInt()
                require(contentLen in 0..buffer.remaining()) { "Invalid save archive" }
                val content = ByteArray(contentLen)
                buffer.get(content)

                // Guard against path traversal: keep only the bare file name.
                val name = nameBytes.decodeToString().substringAfterLast('/')
                if (name.isNotEmpty()) {
                    file("$savesPath/$name").writeBytes(content, false)
                }
            }
        } catch (e: Exception) {
            runCatching { saveDirHandle.deleteDirectory() }
            throw e
        }

        if (!isSaveDir(saveDirHandle)) {
            runCatching { saveDirHandle.deleteDirectory() }
            throw IllegalArgumentException("Imported archive is not a valid save")
        }

        return newDir
    }

    override fun deleteSave(gameDataFolder: String, saveDir: String) {
        val savePath = getSavePath(gameDataFolder, saveDir)
        val handle = file(savePath)

        try {
            handle.deleteDirectory()
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Couldn't delete $savePath", e)
        }
    }

    companion object {
        private const val TAG = "SaveDataRepositoryImpl"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val MAP_SAVE_VERSION: UByte = 7u
        private const val SAVES_DIR = "saves"
        private const val IMPORTED_SAVE_DIR = "imported"

        private val ARCHIVE_MAGIC = "CDA1".encodeToByteArray()
        private const val MAX_ARCHIVE_ENTRIES = 64
        private const val MAX_ARCHIVE_NAME_LENGTH = 255
        private const val DROP_FILE = "drop.dat"
        private const val PROJECTILES_FILE = "projectiles.dat"
        private const val GROW_BLOCKS_FILE = "grow_blocks.dat"
        private const val FIRE_FILE = "fire.dat.gz"
        private const val MOBS_FILE = "mobs.dat"
        private const val CONTAINERS_FILE = "containers.dat"
        private const val DICT_FILE = "dict"
        private const val FOREMAP_FILE = "foremap.dat.gz"
        private const val BACKMAP_FILE = "backmap.dat.gz"
        private const val BIOMES_FILE = "biomes.dat.gz"
        private const val META_FILE = "meta.dat"
        private const val CHUNKS_DIR = "chunks"

        private const val SCREENSHOT_FILE = "screenshot.png"
    }
}
