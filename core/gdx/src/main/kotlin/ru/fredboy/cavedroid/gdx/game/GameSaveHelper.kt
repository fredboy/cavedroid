package ru.fredboy.cavedroid.gdx.game

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.data.save.streaming.ChunkEntityStreamer
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.save.PreparedSave
import ru.fredboy.cavedroid.domain.save.model.FireEntry
import ru.fredboy.cavedroid.domain.save.persist
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.explosion.ExplosionController
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.controller.stats.StatsController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.task.GameWorldGrowBlocksControllerTask
import javax.inject.Inject

@GameScope
class GameSaveHelper @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val dropController: DropController,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val containerController: ContainerController,
    private val saveDataRepository: SaveDataRepository,
    private val projectileController: ProjectileController,
    private val statsController: StatsController,
    private val growBlocksControllerTask: GameWorldGrowBlocksControllerTask,
    private val fireController: FireController,
    private val explosionController: ExplosionController,
    private val chunkEntityStreamer: ChunkEntityStreamer,
    private val vehicles: VehicleController,
    private val dispatchers: AppDispatchers,
) {

    private var secondsSinceLastSave = 0f
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private var pending: PendingSave? = null
    private var disposed = false

    fun updateAutoSave(delta: Float) {
        if (disposed) return
        val active = pending
        if (active != null) {
            if (active.result.isCompleted) drain()
            return
        }
        secondsSinceLastSave += delta
        if (secondsSinceLastSave < AUTO_SAVE_INTERVAL_SEC) return
        secondsSinceLastSave = 0f
        try {
            val prepared = prepareGame(overwrite = true)
            if (dispatchers.io === dispatchers.main) {
                prepared.persist()
                statsController.onSaveCheckpoint()
            } else {
                pending = PendingSave(prepared, scope.async { runCatching { prepared.write() } })
            }
        } catch (error: Throwable) {
            logger.e(error) { "Automatic save failed" }
        }
    }

    fun drain() {
        val active = pending ?: return
        val result = runBlocking { active.result.await() }
        pending = null
        try {
            active.prepared.finish()
            result.fold(
                onSuccess = { statsController.onSaveCheckpoint() },
                onFailure = { error -> logger.e(error) { "Automatic save failed" } },
            )
        } finally {
            secondsSinceLastSave = 0f
        }
    }

    fun dispose() {
        drain()
        disposed = true
        scope.cancel()
    }

    fun saveGame(overwrite: Boolean) {
        drain()
        prepareGame(overwrite).persist()
        statsController.onSaveCheckpoint()
        secondsSinceLastSave = 0f
    }

    private fun prepareGame(overwrite: Boolean): PreparedSave {
        val saves = mutableListOf<PreparedSave>()
        try {
            prepareGame(overwrite, saves)
            return object : PreparedSave {
                private var finished = false

                override fun write() {
                    saves.forEach { it.write() }
                }

                override fun finish() {
                    if (finished) return
                    finished = true
                    saves.forEach { it.finish() }
                }
            }
        } catch (error: Throwable) {
            saves.forEach { it.finish() }
            throw error
        }
    }

    private fun prepareGame(overwrite: Boolean, saves: MutableList<PreparedSave>) {
        val actualSaveDir = saveDataRepository.getActualSaveDirName(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            overwrite = overwrite,
        )

        gameContextRepository.setSaveGameDirectory(actualSaveDir)

        val isInfinite = gameWorld.isInfinite

        if (isInfinite) {
            saves += chunkEntityStreamer.prepareAll()
            saves += fireController.prepareChunks()
            saves += growBlocksControllerTask.prepareChunks()
        }

        saves += saveDataRepository.prepareSave(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            worldName = gameContextRepository.getWorldName(),
            dropController = dropController,
            mobController = mobController,
            containerController = containerController,
            gameWorld = gameWorld,
            projectileController = projectileController,
            tntEntries = explosionController.snapshot(),
            vehicles = if (isInfinite) emptyList() else vehicles.vehicles.map { it.snapshot() },
            growBlockEntries = if (isInfinite) emptyList() else growBlocksControllerTask.snapshot(),
            fireEntries = if (isInfinite) {
                emptyList()
            } else {
                fireController.snapshot().map {
                    FireEntry(x = it.x, y = it.y, layer = it.layer, age = it.age)
                }
            },
        )
    }

    private class PendingSave(val prepared: PreparedSave, val result: Deferred<Result<Unit>>)

    companion object {
        private const val AUTO_SAVE_INTERVAL_SEC = 60f
        private val logger = Logger.withTag("GameSaveHelper")
    }
}
