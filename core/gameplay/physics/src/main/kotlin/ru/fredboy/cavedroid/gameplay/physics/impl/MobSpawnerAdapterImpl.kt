package ru.fredboy.cavedroid.gameplay.physics.impl

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.entity.container.abstraction.MobSpawnerAdapter
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.abs
import kotlin.random.Random

@GameScope
class MobSpawnerAdapterImpl @Inject constructor(
    private val worldProvider: Provider<GameWorld>,
    private val mobsProvider: Provider<MobController>,
    private val factory: MobFactory,
    private val paramsRepository: MobParamsRepository,
) : MobSpawnerAdapter {

    override fun isPlayerNearby(coordinates: ContainerCoordinates): Boolean {
        val world = worldProvider.get()
        val player = mobsProvider.get().player
        if (player.isDead) return false
        val dx = distanceX(player.position.x, coordinates.x + 0.5f, world)
        val dy = player.position.y - coordinates.y - 0.5f
        return dx * dx + dy * dy <= 16f * 16f
    }

    override fun trySpawn(coordinates: ContainerCoordinates, mobKey: String): Boolean = trySpawn(coordinates, mobKey, Random.Default)

    internal fun trySpawn(coordinates: ContainerCoordinates, mobKey: String, random: Random): Boolean {
        val params = paramsRepository.getMobParamsByKey(mobKey) ?: return false
        val world = worldProvider.get()
        val controller = mobsProvider.get()
        // Include mobs queued earlier in this wave (and by neighbouring spawners) in the cap.
        controller.flushPendingSpawns { true }
        if (controller.mobs.count {
                !it.isDead &&
                    it.params.key == mobKey &&
                    distanceX(it.position.x, coordinates.x + 0.5f, world) <= 8f &&
                    abs(it.position.y - coordinates.y - 0.5f) <= 8f
            } >= 6
        ) {
            return false
        }

        repeat(12) {
            val rawX = coordinates.x + random.nextInt(-4, 5)
            val bx = if (world.isInfinite) rawX else Math.floorMod(rawX, world.width)
            val floorY = coordinates.y + random.nextInt(-1, 3)
            if (!world.isChunkLoadedAt(bx) || floorY !in 1 until world.height) return@repeat
            val floor = world.getForeMap(bx, floorY)
            if (!floor.params.hasCollision) return@repeat
            val floorRect = floor.getRectangle(bx, floorY)
            val x = bx + 0.5f
            val y = floorRect.y - params.height / 2f
            val hitbox = Rectangle(x - params.width / 2f, y - params.height / 2f, params.width, params.height)
            if (hitbox.y < 0f || hitbox.y + hitbox.height > world.height) return@repeat
            var blocked = false
            forEachBlockInArea(hitbox) { cellX, cellY ->
                val block = world.getForeMap(cellX, cellY)
                if (!world.isChunkLoadedAt(cellX) ||
                    block.isFluid() ||
                    block.params.damage > 0 ||
                    block.params.hasCollision &&
                    block.getRectangle(cellX, cellY).overlaps(hitbox)
                ) {
                    blocked = true
                }
            }
            if (blocked || !floorRect.overlaps(Rectangle(hitbox.x, floorRect.y, hitbox.width, 0.01f))) return@repeat
            if (world.getLightAt(MathUtils.floor(x), MathUtils.floor(y)) > params.maxSpawnLight) return@repeat
            if (controller.player.hitbox.overlaps(hitbox) || controller.mobs.any { it.hitbox.overlaps(hitbox) }) return@repeat
            if (factory.create(x, y, mobKey) != null) return true
        }
        return false
    }

    private fun distanceX(a: Float, b: Float, world: GameWorld): Float {
        val distance = abs(a - b)
        if (world.isInfinite) return distance
        val wrapped = distance % world.width
        return minOf(wrapped, world.width - wrapped)
    }
}
