package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.factory.MobFactory
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
        Gdx.app.log(TAG, "Spawn controller task started")
        var spawnCount = 0
        if (!gameWorld.isDayTime() || mobController.mobs.size < maintainedMobsCount) {
            val mobKeys = mobParamsRepository.getMobKeysByBehaviorType(
                if (gameWorld.isDayTime()) MobBehaviorType.PASSIVE else MobBehaviorType.AGGRESSIVE,
            )

            if (mobKeys.isNotEmpty()) {
                for (x in 0..<gameWorld.width step SPAWN_CHUNK_SIZE) {
                    val spawnX = x + MathUtils.random(SPAWN_CHUNK_SIZE)
                    var y = 0
                    while (++y < gameWorld.generatorConfig.seaLevel) {
                        if (gameWorld.getForeMap(spawnX, y).params.hasCollision) {
                            break
                        }
                    }

                    if (gameWorld.getForeMap(spawnX, y).params.hasCollision) {
                        mobFactory.create(spawnX.toFloat(), y.toFloat(), mobKeys.random())
                        spawnCount++
                    }
                }
            }
        }
        Gdx.app.log(TAG, "Spawn controller task finished. Spawn count: $spawnCount")
    }

    companion object {
        private const val TAG = "GameWorldMobSpawnControllerTask"

        private const val SPAWN_CHUNK_SIZE = 64

        const val SPAWN_INTERVAL_SEC = GameWorld.DAY_DURATION_SEC / 4f
    }
}
