package ru.fredboy.cavedroid.domain.save.repository

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.save.model.ChunkEntitiesSaveData
import ru.fredboy.cavedroid.domain.save.model.ChunkSaveData
import ru.fredboy.cavedroid.domain.save.model.FireEntry
import ru.fredboy.cavedroid.domain.save.model.GameMapSaveData
import ru.fredboy.cavedroid.domain.save.model.GameSaveDetails
import ru.fredboy.cavedroid.domain.save.model.GameSaveInfo
import ru.fredboy.cavedroid.domain.save.model.GrowBlockEntry
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.MobQueue
import ru.fredboy.cavedroid.entity.mob.model.Mob
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

interface SaveDataRepository {

    fun getActualSaveDirName(
        gameDataFolder: String,
        saveGameDirectory: String,
        overwrite: Boolean,
    ): String

    fun save(
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
    )

    fun loadGrowBlockEntries(
        gameDataFolder: String,
        saveGameDirectory: String,
    ): List<GrowBlockEntry>

    fun loadFireEntries(
        gameDataFolder: String,
        saveGameDirectory: String,
    ): List<FireEntry>

    fun loadMap(
        gameDataFolder: String,
        saveGameDirectory: String,
    ): GameMapSaveData

    /** Persists a single infinite-world chunk's block data. */
    fun saveInfiniteChunk(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
        foreMap: Array<Array<Block>>,
        backMap: Array<Array<Block>>,
        biomes: Array<Biome>,
    )

    /** Loads a previously-persisted infinite-world chunk, or null if it was never modified/saved. */
    fun loadInfiniteChunk(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
    ): ChunkSaveData?

    /** Persists the mobs/drops/containers belonging to one infinite-world chunk (deletes the file when empty). */
    fun saveChunkEntities(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
        mobs: List<Mob>,
        drops: List<Drop>,
        containers: Map<ContainerCoordinates, Container>,
    )

    /** Loads and reconstructs the entities of one infinite-world chunk, or null if none were saved. */
    fun loadChunkEntities(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
        mobPhysicsFactory: MobPhysicsFactory,
        dropWorldAdapter: DropWorldAdapter,
    ): ChunkEntitiesSaveData?

    /** Persists the fire instances of one infinite-world chunk (deletes the file when empty). */
    fun saveChunkFire(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
        entries: List<FireEntry>,
    )

    fun loadChunkFire(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
    ): List<FireEntry>

    /** Persists the pending grow-block timers of one infinite-world chunk (deletes the file when empty). */
    fun saveChunkGrowBlocks(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
        entries: List<GrowBlockEntry>,
    )

    fun loadChunkGrowBlocks(
        gameDataFolder: String,
        saveGameDirectory: String,
        chunkX: Int,
    ): List<GrowBlockEntry>

    fun loadContainerController(
        gameDataFolder: String,
        saveGameDirectory: String,
        containerWorldAdapter: ContainerWorldAdapter,
        containerFactory: ContainerFactory,
        dropAdapter: DropAdapter,
    ): ContainerController

    fun loadDropController(
        gameDataFolder: String,
        saveGameDirectory: String,
        dropWorldAdapter: DropWorldAdapter,
        dropQueue: DropQueue,
        playerAdapter: PlayerAdapter,
    ): DropController

    fun loadMobController(
        gameDataFolder: String,
        saveGameDirectory: String,
        mobWorldAdapter: MobWorldAdapter,
        mobPhysicsFactory: MobPhysicsFactory,
        dropQueue: DropQueue,
        mobSoundManager: MobSoundManager,
        projectileAdapter: ProjectileAdapter,
        mobQueue: MobQueue,
    ): MobController

    fun loadProjectileController(
        gameDataFolder: String,
        saveGameDirectory: String,
        projectileWorldAdapter: ProjectileWorldAdapter,
        dropQueue: DropQueue,
    ): ProjectileController

    fun getSavesInfo(gameDataFolder: String): List<GameSaveInfo>

    /** Directories that look like saves but whose data failed to load. */
    fun findCorruptedSaveDirectories(gameDataFolder: String): List<String>

    fun getSaveDetails(gameDataFolder: String, saveDir: String): GameSaveDetails

    fun renameSave(gameDataFolder: String, saveDir: String, newName: String)

    fun exportSaveToZip(gameDataFolder: String, saveDir: String): ByteArray

    /** Imports a zipped save into a new slot and returns the new directory name. */
    fun importSaveFromZip(gameDataFolder: String, zipBytes: ByteArray): String

    fun deleteSave(gameDataFolder: String, saveDir: String)
}
