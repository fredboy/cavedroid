package ru.fredboy.cavedroid.data.save.repository

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
import ru.fredboy.cavedroid.data.save.mapper.ContainerControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.DropControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.GameSaveInfoMapper
import ru.fredboy.cavedroid.data.save.mapper.MobControllerMapper
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.save.model.GameMapSaveData
import ru.fredboy.cavedroid.domain.save.model.GameSaveInfo
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
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
) : SaveDataRepository {

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

    private fun internalLoadMap(
        savesPath: String,
    ): GameMapSaveData {
        val dict = Gdx.files.absolute("$savesPath/$DICT_FILE").readString().split("\n")

        val foreMap: Array<Array<Block>>
        with(GZIPInputStream(Gdx.files.absolute("$savesPath/$FOREMAP_FILE").read())) {
            foreMap = decompressMap(readBytes(), dict, itemsRepository)
            close()
        }

        val backMap: Array<Array<Block>>
        with(GZIPInputStream(Gdx.files.absolute("$savesPath/$BACKMAP_FILE").read())) {
            backMap = decompressMap(readBytes(), dict, itemsRepository)
            close()
        }

        val meta = loadMapData(savesPath)

        return GameMapSaveData(
            foreMap = foreMap,
            backMap = backMap,
            gameTime = meta.gameTime,
            moonPhase = meta.moonPhase,
        )
    }

    private fun saveMap(gameWorld: GameWorld, savesPath: String) {
        val fullForeMap = gameWorld.foreMap
        val fullBackMap = gameWorld.backMap

        val dict = buildBlocksDictionary(fullForeMap, fullBackMap)

        saveDict(Gdx.files.absolute("$savesPath/$DICT_FILE"), dict)

        with(GZIPOutputStream(Gdx.files.absolute("$savesPath/$FOREMAP_FILE").write(false))) {
            write(compressMap(fullForeMap, dict))
            close()
        }

        with(GZIPOutputStream(Gdx.files.absolute("$savesPath/$BACKMAP_FILE").write(false))) {
            write(compressMap(fullBackMap, dict))
            close()
        }
    }

    private fun saveMapData(gameWorld: GameWorld, savesPath: String, worldName: String, gameMode: GameMode) {
        val metaFile = Gdx.files.absolute("$savesPath/$META_FILE")

        val worldSaveDataDto = SaveDataDto.WorldSaveDataDto(
            version = MAP_SAVE_VERSION.toInt(),
            name = worldName,
            timestamp = TimeUtils.millis(),
            gameTime = gameWorld.currentGameTime,
            moonPhase = gameWorld.moonPhase,
            gameMode = gameMode,
        )

        val bytes = ProtoBuf.encodeToByteArray(worldSaveDataDto)

        metaFile.writeBytes(bytes, false)
    }

    private fun loadMapData(savesPath: String): SaveDataDto.WorldSaveDataDto {
        val metaFile = Gdx.files.absolute("$savesPath/$META_FILE")

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

        PixmapIO.writePNG(Gdx.files.absolute("$savesPath/$SCREENSHOT_FILE"), pixmap, Deflater.DEFAULT_COMPRESSION, true)
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
        var saveDirHandle = Gdx.files.absolute(savesPath)
        var suffix = 0

        while (saveDirHandle.exists() && suffix < 256) {
            savesPath = getSavePath(gameDataFolder, "${saveGameDirectory}_${++suffix}")
            saveDirHandle = Gdx.files.absolute(savesPath)
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
    ) {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)

        val dropFile = Gdx.files.absolute("$savesPath/$DROP_FILE")
        val mobsFile = Gdx.files.absolute("$savesPath/$MOBS_FILE")
        val containersFile = Gdx.files.absolute("$savesPath/$CONTAINERS_FILE")

        val dropBytes = ProtoBuf.encodeToByteArray(dropControllerMapper.mapSaveData(dropController))
        val mobsBytes = ProtoBuf.encodeToByteArray(mobControllerMapper.mapSaveData(mobController))
        val containersBytes =
            ProtoBuf.encodeToByteArray(containerControllerMapper.mapSaveData(containerController))

        dropFile.writeBytes(dropBytes, false)
        mobsFile.writeBytes(mobsBytes, false)
        containersFile.writeBytes(containersBytes, false)

        saveMap(gameWorld, savesPath)

        saveMapData(gameWorld, savesPath, worldName, mobController.player.gameMode)

        takeScreenshot(savesPath)
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
        val containersFile = Gdx.files.absolute("$savesPath/$CONTAINERS_FILE")
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
        val dropFile = Gdx.files.absolute("$savesPath/$DROP_FILE")
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
    ): MobController {
        val savesPath = getSavePath(gameDataFolder, saveGameDirectory)
        val mobsFile = Gdx.files.absolute("$savesPath/$MOBS_FILE")
        val mobsBytes = mobsFile.readBytes()

        return ProtoBuf.decodeFromByteArray<SaveDataDto.MobControllerSaveDataDto>(mobsBytes)
            .let { saveData ->
                mobControllerMapper.mapMobController(
                    saveDataDto = saveData,
                    mobWorldAdapter = mobWorldAdapter,
                    mobPhysicsFactory = mobPhysicsFactory,
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

        val requiredFiles = setOf(
            DROP_FILE,
            MOBS_FILE,
            CONTAINERS_FILE,
            DICT_FILE,
            FOREMAP_FILE,
            BACKMAP_FILE,
            META_FILE,
        )

        return requiredFiles.all { it in files }
    }

    override fun getSavesInfo(gameDataFolder: String): List<GameSaveInfo> {
        return Gdx.files.absolute("$gameDataFolder/$SAVES_DIR").list { it.isDirectory }
            .asSequence()
            .filter { isSaveDir(it) }
            .map { saveDir ->
                val saveData = loadMapData(saveDir.path())
                gameSaveInfoMapper.map(
                    dto = saveData,
                    dir = saveDir.name(),
                    expectedVersion = MAP_SAVE_VERSION.toInt(),
                    screenshotHandle = saveDir.child(SCREENSHOT_FILE).takeIf { it.exists() },
                )
            }
            .take(MAX_SAVES_COUNT)
            .toList()
            .sortedByDescending { it.lastModifiedTimestamp }
    }

    override fun deleteSave(gameDataFolder: String, saveDir: String) {
        val savePath = getSavePath(gameDataFolder, saveDir)
        val handle = Gdx.files.absolute(savePath)

        try {
            handle.deleteDirectory()
        } catch (e: Exception) {
            Gdx.app.error(TAG, "Couldn't delete $savePath", e)
        }
    }

    companion object {
        private const val TAG = "SaveDataRepositoryImpl"

        private const val MAP_SAVE_VERSION: UByte = 6u
        private const val SAVES_DIR = "saves"
        private const val DROP_FILE = "drop.dat"
        private const val MOBS_FILE = "mobs.dat"
        private const val CONTAINERS_FILE = "containers.dat"
        private const val DICT_FILE = "dict"
        private const val FOREMAP_FILE = "foremap.dat.gz"
        private const val BACKMAP_FILE = "backmap.dat.gz"
        private const val META_FILE = "meta.dat"

        private const val SCREENSHOT_FILE = "screenshot.png"
    }
}
