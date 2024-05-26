package ru.deadsoftware.cavedroid.game.save

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto
import ru.deadsoftware.cavedroid.game.objects.container.ContainerController
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.game.ui.TooltipManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.domain.assets.usecase.GetPigSpritesUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetPlayerSpritesUseCase
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@OptIn(ExperimentalSerializationApi::class)
object GameSaveLoader {

    private const val MAP_SAVE_VERSION: UByte = 2u

    private const val SAVES_DIR = "/saves"
    private const val DROP_FILE = "/drop.dat"
    private const val MOBS_FILE = "/mobs.dat"
    private const val CONTAINERS_FILE = "/containers.dat"
    private const val DICT_FILE = "/dict"
    private const val FOREMAP_FILE = "/foremap.dat.gz"
    private const val BACKMAP_FILE = "/backmap.dat.gz"

    private fun Int.toByteArray(): ByteArray {
        return ByteBuffer.allocate(Int.SIZE_BYTES)
            .putInt(this)
            .array()
    }

    private fun Short.toByteArray(): ByteArray {
        return ByteBuffer.allocate(Short.SIZE_BYTES)
            .putShort(this)
            .array()
    }

    private fun buildBlocksDictionary(
        foreMap: Array<Array<Block>>,
        backMap: Array<Array<Block>>
    ): Map<String, Int> {
        val maps = sequenceOf(foreMap.asSequence(), backMap.asSequence())

        return maps.flatten()
            .flatMap(Array<Block>::asSequence)
            .toSet()
            .mapIndexed { index, block -> block.params.key to index }
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
            for (y in 0 ..< height) {
                for (x in 0 ..< width) {
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
        gameItemsHolder: GameItemsHolder
    ): Array<Array<Block>> {
        val version = bytes.first().toUByte()
        require(version == MAP_SAVE_VERSION)

        val width = ByteBuffer.wrap(bytes, 1, Int.SIZE_BYTES).getInt()
        val height = ByteBuffer.wrap(bytes, 1 + Int.SIZE_BYTES, Int.SIZE_BYTES).getInt()

        val blocks = buildList {
            for (i in 1 + (Int.SIZE_BYTES shl 1) .. bytes.lastIndex step Int.SIZE_BYTES + 1) {
                val run = ByteBuffer.wrap(bytes, i, Int.SIZE_BYTES).getInt()
                val blockId = bytes[i + Int.SIZE_BYTES].toUByte().toInt()

                for (j in 0 ..< run) {
                    add(gameItemsHolder.getBlock(dict[blockId]))
                }
            }
        }

        return Array(width) { x ->
            Array(height) { y ->
                blocks[x + y * width]
            }
        }
    }

    private fun loadMap(
        gameItemsHolder: GameItemsHolder,
        savesPath: String
    ): Pair<Array<Array<Block>>, Array<Array<Block>>> {
        val dict = Gdx.files.absolute("$savesPath$DICT_FILE").readString().split("\n")

        val foreMap: Array<Array<Block>>
        with(GZIPInputStream(Gdx.files.absolute("$savesPath$FOREMAP_FILE").read())) {
            foreMap = decompressMap(readBytes(), dict, gameItemsHolder)
            close()
        }

        val backMap: Array<Array<Block>>
        with(GZIPInputStream(Gdx.files.absolute("$savesPath$BACKMAP_FILE").read())) {
            backMap = decompressMap(readBytes(), dict, gameItemsHolder)
            close()
        }

        return foreMap to backMap
    }

    private fun saveMap(gameWorld: GameWorld, savesPath: String) {
        val fullForeMap = gameWorld.fullForeMap
        val fullBackMap = gameWorld.fullBackMap

        val dict = buildBlocksDictionary(fullForeMap, fullBackMap)

        saveDict(Gdx.files.absolute("$savesPath$DICT_FILE"), dict)

        with(GZIPOutputStream(Gdx.files.absolute("$savesPath$FOREMAP_FILE").write(false))) {
            write(compressMap(fullForeMap, dict))
            close()
        }

        with(GZIPOutputStream(Gdx.files.absolute("$savesPath$BACKMAP_FILE").write(false))) {
            write(compressMap(fullBackMap, dict))
            close()
        }
    }

    fun load(
        mainConfig: MainConfig,
        gameItemsHolder: GameItemsHolder,
        tooltipManager: TooltipManager,
        getPlayerSprites: GetPlayerSpritesUseCase,
        getPigSprites: GetPigSpritesUseCase,
    ): GameSaveData {
        val gameFolder = mainConfig.gameFolder
        val savesPath = "$gameFolder$SAVES_DIR"

        val dropFile = Gdx.files.absolute("$savesPath$DROP_FILE")
        val mobsFile = Gdx.files.absolute("$savesPath$MOBS_FILE")
        val containersFile = Gdx.files.absolute("$savesPath$CONTAINERS_FILE")

        val dropBytes = dropFile.readBytes()
        val mobsBytes = mobsFile.readBytes()
        val containersBytes = containersFile.readBytes()

        val dropController = ProtoBuf.decodeFromByteArray<SaveDataDto.DropControllerSaveData>(dropBytes)
            .let { saveData ->
                DropController.fromSaveData(
                    /* saveData = */ saveData,
                    /* gameItemsHolder = */ gameItemsHolder
                )
            }

        val mobsController = ProtoBuf.decodeFromByteArray<SaveDataDto.MobsControllerSaveData>(mobsBytes)
            .let { saveData ->
                MobsController.fromSaveData(
                    saveData = saveData,
                    gameItemsHolder = gameItemsHolder,
                    tooltipManager = tooltipManager,
                    getPigSprites = getPigSprites,
                    getPlayerSprites = getPlayerSprites
                )
            }

        val containerController = ProtoBuf.decodeFromByteArray<SaveDataDto.ContainerControllerSaveData>(containersBytes)
            .let { saveData ->
                ContainerController.fromSaveData(
                    saveData = saveData,
                    dropController = dropController,
                    gameItemsHolder = gameItemsHolder
                )
            }

        val (foreMap, backMap) = loadMap(gameItemsHolder, savesPath)

        return GameSaveData(mobsController, dropController, containerController, foreMap, backMap)
    }

    fun save(
        mainConfig: MainConfig,
        dropController: DropController,
        mobsController: MobsController,
        containerController: ContainerController,
        gameWorld: GameWorld
    ) {
        val gameFolder = mainConfig.gameFolder
        val savesPath = "$gameFolder$SAVES_DIR"

        Gdx.files.absolute(savesPath).mkdirs()

        val dropFile = Gdx.files.absolute("$savesPath$DROP_FILE")
        val mobsFile = Gdx.files.absolute("$savesPath$MOBS_FILE")
        val containersFile = Gdx.files.absolute("$savesPath$CONTAINERS_FILE")

        val dropBytes = ProtoBuf.encodeToByteArray(dropController.getSaveData())
        val mobsBytes = ProtoBuf.encodeToByteArray(mobsController.getSaveData())
        val containersBytes = ProtoBuf.encodeToByteArray(containerController.getSaveData())

        dropFile.writeBytes(dropBytes, false)
        mobsFile.writeBytes(mobsBytes, false)
        containersFile.writeBytes(containersBytes, false)

        saveMap(gameWorld, savesPath)
    }

    fun exists(mainConfig: MainConfig): Boolean {
        val gameFolder = mainConfig.gameFolder
        val savesPath = "$gameFolder$SAVES_DIR"

        return Gdx.files.absolute("$savesPath$DROP_FILE").exists() &&
                Gdx.files.absolute("$savesPath$MOBS_FILE").exists() &&
                Gdx.files.absolute("$savesPath$CONTAINERS_FILE").exists() &&
                Gdx.files.absolute("$savesPath$DICT_FILE").exists() &&
                Gdx.files.absolute("$savesPath$FOREMAP_FILE").exists() &&
                Gdx.files.absolute("$savesPath$BACKMAP_FILE").exists()
    }


}