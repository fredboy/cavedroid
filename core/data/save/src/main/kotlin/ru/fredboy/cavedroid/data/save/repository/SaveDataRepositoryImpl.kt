package ru.fredboy.cavedroid.data.save.repository

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import ru.fredboy.cavedroid.data.save.mapper.ContainerControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.DropControllerMapper
import ru.fredboy.cavedroid.data.save.mapper.MobControllerMapper
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.save.model.GameSaveData
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject

internal class SaveDataRepositoryImpl @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val dropControllerMapper: DropControllerMapper,
    private val containerControllerMapper: ContainerControllerMapper,
    private val mobControllerMapper: MobControllerMapper,
) : SaveDataRepository {

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
        itemsRepository: ItemsRepository,
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


    private fun loadMap(
        itemsRepository: ItemsRepository,
        savesPath: String
    ): Pair<Array<Array<Block>>, Array<Array<Block>>> {
        val dict = Gdx.files.absolute("$savesPath$DICT_FILE").readString().split("\n")

        val foreMap: Array<Array<Block>>
        with(GZIPInputStream(Gdx.files.absolute("$savesPath$FOREMAP_FILE").read())) {
            foreMap = decompressMap(readBytes(), dict, itemsRepository)
            close()
        }

        val backMap: Array<Array<Block>>
        with(GZIPInputStream(Gdx.files.absolute("$savesPath$BACKMAP_FILE").read())) {
            backMap = decompressMap(readBytes(), dict, itemsRepository)
            close()
        }

        return foreMap to backMap
    }

    private fun saveMap(gameWorld: GameWorld, savesPath: String) {
        val fullForeMap = gameWorld.foreMap
        val fullBackMap = gameWorld.backMap

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

    override fun save(
        gameDataFolder: String,
        dropController: DropController,
        mobController: MobController,
        containerController: ContainerController,
        gameWorld: GameWorld
    ) {
        val savesPath = "$gameDataFolder$SAVES_DIR"

        Gdx.files.absolute(savesPath).mkdirs()

        val dropFile = Gdx.files.absolute("$savesPath$DROP_FILE")
        val mobsFile = Gdx.files.absolute("$savesPath$MOBS_FILE")
        val containersFile = Gdx.files.absolute("$savesPath$CONTAINERS_FILE")

        val dropBytes = ProtoBuf.encodeToByteArray(dropControllerMapper.mapSaveData(dropController as DropController))
        val mobsBytes = ProtoBuf.encodeToByteArray(mobControllerMapper.mapSaveData(mobController as MobController))
        val containersBytes = ProtoBuf.encodeToByteArray(containerControllerMapper.mapSaveData(containerController as ContainerController))

        dropFile.writeBytes(dropBytes, false)
        mobsFile.writeBytes(mobsBytes, false)
        containersFile.writeBytes(containersBytes, false)

        saveMap(gameWorld, savesPath)
    }

    override fun load(gameDataFolder: String): GameSaveData {
        val savesPath = "$gameDataFolder$SAVES_DIR"

        val dropFile = Gdx.files.absolute("$savesPath$DROP_FILE")
        val mobsFile = Gdx.files.absolute("$savesPath$MOBS_FILE")
        val containersFile = Gdx.files.absolute("$savesPath$CONTAINERS_FILE")

        val dropBytes = dropFile.readBytes()
        val mobsBytes = mobsFile.readBytes()
        val containersBytes = containersFile.readBytes()

        val dropController = ProtoBuf.decodeFromByteArray<SaveDataDto.DropControllerSaveDataDto>(dropBytes)
            .let { saveData ->
                dropControllerMapper.mapDropController(saveData)
            }

        val mobController = ProtoBuf.decodeFromByteArray<SaveDataDto.MobControllerSaveDataDto>(mobsBytes)
            .let { saveData ->
                mobControllerMapper.mapMobController(saveData)
            }

        val containerController = ProtoBuf.decodeFromByteArray<SaveDataDto.ContainerControllerSaveDataDto>(containersBytes)
            .let { saveData ->
                containerControllerMapper.mapContainerController(saveData)
            }

        val (foreMap, backMap) = loadMap(itemsRepository, savesPath)

        return GameSaveData(mobController, dropController, containerController, foreMap, backMap)
    }

    override fun exists(gameDataFolder: String,): Boolean {
        val savesPath = "$gameDataFolder$SAVES_DIR"

        return Gdx.files.absolute("$savesPath$DROP_FILE").exists() &&
                Gdx.files.absolute("$savesPath$MOBS_FILE").exists() &&
                Gdx.files.absolute("$savesPath$CONTAINERS_FILE").exists() &&
                Gdx.files.absolute("$savesPath$DICT_FILE").exists() &&
                Gdx.files.absolute("$savesPath$FOREMAP_FILE").exists() &&
                Gdx.files.absolute("$savesPath$BACKMAP_FILE").exists()
    }

    companion object {
        private const val MAP_SAVE_VERSION: UByte = 2u

        private const val SAVES_DIR = "/saves"
        private const val DROP_FILE = "/drop.dat"
        private const val MOBS_FILE = "/mobs.dat"
        private const val CONTAINERS_FILE = "/containers.dat"
        private const val DICT_FILE = "/dict"
        private const val FOREMAP_FILE = "/foremap.dat.gz"
        private const val BACKMAP_FILE = "/backmap.dat.gz"
    }
}