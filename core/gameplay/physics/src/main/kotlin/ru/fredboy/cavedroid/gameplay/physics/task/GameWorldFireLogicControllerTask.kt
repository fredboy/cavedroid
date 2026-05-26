package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.math.MathUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.fire.FireSpreadRules
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.math.min

/**
 * Ticks every [FIRE_UPDATE_INTERVAL_SEC]. Runs on the libGDX Timer thread —
 * reads the world map directly (consistent with the existing fluids/blocks
 * tasks) but defers every world mutation to the libGDX main thread via
 * [AppDispatchers.main] (which wraps `Gdx.app.postRunnable`). Mutations are
 * batched into a single main-thread runnable per tick to avoid per-fire
 * postRunnable churn.
 */
@GameScope
class GameWorldFireLogicControllerTask @Inject constructor(
    private val gameWorld: GameWorld,
    private val fireController: FireController,
    private val mobController: MobController,
    private val appDispatchers: AppDispatchers,
) : BaseGameWorldControllerTask() {

    override fun exec() {
        val pending = mutableListOf<() -> Unit>()

        fireController.snapshot().forEach { fire ->
            val support = blockAt(fire.x, fire.y, fire.layer)

            if (!FireSpreadRules.supportStillValid(support)) {
                pending += { fireController.removeFire(fire.x, fire.y, fire.layer) }
                return@forEach
            }

            if (isWaterNearby(fire.x, fire.y)) {
                pending += { fireController.removeFire(fire.x, fire.y, fire.layer) }
                return@forEach
            }

            val nextAge = fire.age + FIRE_UPDATE_INTERVAL_SEC

            if (nextAge >= FIRE_LIFETIME_SEC) {
                val (x, y, layer) = Triple(fire.x, fire.y, fire.layer)
                pending += {
                    fireController.removeFire(x, y, layer)
                    when (layer) {
                        Layer.FOREGROUND -> gameWorld.destroyForeMap(x, y, shouldDrop = false, destroyedByPlayer = false)
                        Layer.BACKGROUND -> gameWorld.destroyBackMap(x, y, shouldDrop = false, destroyedByPlayer = false)
                    }
                }
                return@forEach
            }

            pending += { fireController.getFireAt(fire.x, fire.y, fire.layer)?.age = nextAge }

            spreadFromFire(fire.x, fire.y, fire.layer, pending)
        }

        igniteAroundLava(pending)

        if (pending.isEmpty()) return

        CoroutineScope(appDispatchers.main).launch {
            pending.forEach { it() }
        }
    }

    private fun spreadFromFire(x: Int, y: Int, sourceLayer: Layer, pending: MutableList<() -> Unit>) {
        // Cross-layer ignition at the same (x, y).
        Layer.entries
            .filter { it != sourceLayer }
            .forEach { other ->
                tryIgnite(x, y, other, pending)
            }

        NEIGHBOUR_OFFSETS.forEach { (dx, dy) ->
            val nx = x + dx
            val ny = y + dy
            if (ny !in 0..<gameWorld.height) return@forEach

            Layer.entries.forEach { targetLayer ->
                tryIgnite(nx, ny, targetLayer, pending)
            }
        }
    }

    private fun tryIgnite(x: Int, y: Int, layer: Layer, pending: MutableList<() -> Unit>) {
        if (fireController.hasFireAt(x, y, layer)) return
        val target = blockAt(x, y, layer)
        if (FireSpreadRules.shouldSpread(MathUtils.random(), target)) {
            pending += { fireController.addFire(x, y, layer) }
        }
    }

    /**
     * Lava blocks within `LAVA_SCAN_RADIUS` of the player can ignite combustible
     * neighbours in either layer. This is bounded by a small box around the
     * player to keep the per-tick cost predictable.
     */
    private fun igniteAroundLava(pending: MutableList<() -> Unit>) {
        val player = mobController.player
        val centerX = player.mapX
        val centerY = player.middleMapY
        val radiusX = min(gameWorld.width / 2, LAVA_SCAN_RADIUS)

        for (dy in -LAVA_SCAN_RADIUS..LAVA_SCAN_RADIUS) {
            val y = centerY + dy
            if (y !in 0..<gameWorld.height) continue
            for (dx in -radiusX..radiusX) {
                val x = centerX + dx
                if (!gameWorld.getForeMap(x, y).isLava()) continue
                NEIGHBOUR_OFFSETS.forEach { (ox, oy) ->
                    val nx = x + ox
                    val ny = y + oy
                    if (ny !in 0..<gameWorld.height) return@forEach
                    Layer.entries.forEach { layer ->
                        if (fireController.hasFireAt(nx, ny, layer)) return@forEach
                        val target = blockAt(nx, ny, layer)
                        if (FireSpreadRules.canIgnite(target) && MathUtils.random() < LAVA_IGNITION_CHANCE) {
                            pending += { fireController.addFire(nx, ny, layer) }
                        }
                    }
                }
            }
        }
    }

    private fun blockAt(x: Int, y: Int, layer: Layer): Block = when (layer) {
        Layer.FOREGROUND -> gameWorld.getForeMap(x, y)
        Layer.BACKGROUND -> gameWorld.getBackMap(x, y)
    }

    private fun isWaterNearby(x: Int, y: Int): Boolean {
        return NEIGHBOUR_OFFSETS.any { (dx, dy) ->
            val ny = y + dy
            if (ny !in 0..<gameWorld.height) return@any false
            gameWorld.getForeMap(x + dx, ny).isWater()
        }
    }

    companion object {
        const val FIRE_UPDATE_INTERVAL_SEC = 0.5f
        const val FIRE_LIFETIME_SEC = 8f

        private const val LAVA_SCAN_RADIUS = 16
        private const val LAVA_IGNITION_CHANCE = 0.20f

        private val NEIGHBOUR_OFFSETS = listOf(
            -1 to 0,
            1 to 0,
            0 to -1,
            0 to 1,
        )
    }
}
