package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class GameWorldMobSpawnControllerTask @Inject constructor(
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val mobParamsRepository: MobParamsRepository,
    private val mobFactory: MobFactory,
) : BaseGameWorldControllerTask() {

    private val maintainedMobsCount = gameWorld.width / SPAWN_CHUNK_SIZE

    override fun exec() {
        logger.i {
            "Spawn controller task started. " +
                "Current time: ${gameWorld.totalGameTimeSec}. Last spawn time: ${gameWorld.lastSpawnGameTime}"
        }

        val surfaceCandidates = mobParamsRepository.getAllParams()
            .filter {
                when (it.behaviorType) {
                    MobBehaviorType.PASSIVE, MobBehaviorType.SHEEP -> gameWorld.isDayTime()
                    MobBehaviorType.AGGRESSIVE, MobBehaviorType.ARCHER -> !gameWorld.isDayTime()
                    else -> false
                }
            }

        val caveCandidates = mobParamsRepository.getAllParams()
            .filter {
                it.behaviorType == MobBehaviorType.AGGRESSIVE || it.behaviorType == MobBehaviorType.ARCHER
            }

        val canSpawnSurface = (!gameWorld.isDayTime() || mobController.mobs.size < maintainedMobsCount) &&
            surfaceCandidates.isNotEmpty()
        val canSpawnCave = mobController.mobs.size < maintainedMobsCount && caveCandidates.isNotEmpty()

        var spawnCount = 0
        var caveSpawns = 0
        for (x in 0..<gameWorld.width step SPAWN_CHUNK_SIZE) {
            val isDayInDesert = gameWorld.isDayTime() && gameWorld.getBiomeAt(x) == Biome.DESERT
            if (canSpawnSurface && !isDayInDesert) {
                val surfaceX = x + MathUtils.random(SPAWN_CHUNK_SIZE - 1)
                if (trySpawnAtSurface(surfaceX, surfaceCandidates)) {
                    logger.d { "Spawned on surface at x: $surfaceX" }
                    spawnCount++
                }
            }

            if (canSpawnCave) {
                val caveX = x + MathUtils.random(SPAWN_CHUNK_SIZE - 1)
                if (trySpawnInCave(caveX, caveCandidates)) {
                    logger.d { "Spawned in cave at x: $caveX" }
                    caveSpawns++
                    spawnCount++
                }
            }
        }

        gameWorld.lastSpawnGameTime = gameWorld.totalGameTimeSec
        logger.i { "Spawn controller task finished. Spawn count: $spawnCount. Of them in caves: $caveSpawns" }
    }

    private fun trySpawnAtSurface(spawnX: Int, candidates: List<MobParams>): Boolean {
        var surfaceY = -1
        for (y in 1..<gameWorld.generatorConfig.seaLevel) {
            if (gameWorld.getForeMap(spawnX, y).params.hasCollision) {
                surfaceY = y
                break
            }
        }
        if (surfaceY < 0) return false
        if (gameWorld.getForeMap(spawnX, surfaceY).params.key !in SPAWN_BLOCKS) return false

        return trySpawnAbove(spawnX, surfaceY, candidates)
    }

    private fun trySpawnInCave(spawnX: Int, candidates: List<MobParams>): Boolean {
        val caveTop = gameWorld.generatorConfig.seaLevel
        val caveBottom = (gameWorld.generatorConfig.lavaLevel - 2).coerceAtLeast(caveTop + 1)

        repeat(CAVE_SCAN_ATTEMPTS) {
            val airY = MathUtils.random(caveTop, caveBottom)
            if (gameWorld.getForeMap(spawnX, airY).params.hasCollision) {
                return@repeat
            }

            var floorY = -1
            val scanEnd = (airY + CAVE_FLOOR_SCAN_DEPTH).coerceAtMost(gameWorld.height - 1)
            for (y in (airY + 1)..scanEnd) {
                val block = gameWorld.getForeMap(spawnX, y)
                if (block.params.hasCollision) {
                    if (block.params.key in SPAWN_BLOCKS) {
                        floorY = y
                    }
                    break
                }
            }
            if (floorY < 0) return@repeat

            if (trySpawnAbove(spawnX, floorY, candidates)) {
                return true
            }
        }
        return false
    }

    private fun trySpawnAbove(spawnX: Int, floorY: Int, candidates: List<MobParams>): Boolean {
        val params = candidates.random()
        val spawnPosX = spawnX.toFloat()
        val spawnPosY = floorY.toFloat() - params.height / 2f
        if (!canFitAt(spawnPosX, spawnPosY, params)) return false

        mobFactory.create(spawnPosX, spawnPosY, params.key)
        return true
    }

    private fun canFitAt(x: Float, y: Float, params: MobParams): Boolean {
        val hitbox = Rectangle(
            x - params.width / 2f,
            y - params.height / 2f,
            params.width,
            params.height,
        )
        var blocked = false
        forEachBlockInArea(hitbox) { bx, by ->
            if (blocked) return@forEachBlockInArea
            val block = gameWorld.getForeMap(bx, by)
            if (block.params.hasCollision && block.getRectangle(bx, by).overlaps(hitbox)) {
                blocked = true
            }
        }
        return !blocked
    }

    companion object {
        private const val TAG = "GameWorldMobSpawnControllerTask"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val SPAWN_CHUNK_SIZE = 16
        private const val CAVE_SCAN_ATTEMPTS = 8
        private const val CAVE_FLOOR_SCAN_DEPTH = 8

        private val SPAWN_BLOCKS = setOf("dirt", "grass", "grass_snowed", "sand", "stone")

        const val SPAWN_INTERVAL_SEC = GameWorld.DAY_DURATION_SEC / 4f
    }
}
