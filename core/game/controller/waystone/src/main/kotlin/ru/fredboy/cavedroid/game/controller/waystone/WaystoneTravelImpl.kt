package ru.fredboy.cavedroid.game.controller.waystone

import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.waystone.Waystone
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravel
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravelResult
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

@GameScope
class WaystoneTravelImpl @Inject constructor(
    private val world: GameWorld,
    private val mobs: MobController,
    private val vehicles: VehicleController,
    private val context: GameContextRepository,
) : WaystoneTravel,
    OnBlockDestroyedListener {
    private val player get() = mobs.player
    override val playerHealth get() = player.health

    init {
        world.addBlockDestroyedListener(this)
    }

    override fun activate(x: Int, y: Int, defaultName: String): Waystone? {
        val canonicalX = world.containerX(x)
        val layer = when {
            world.getForeMap(canonicalX, y).params.key == KEY -> Layer.FOREGROUND
            world.getBackMap(canonicalX, y).params.key == KEY -> Layer.BACKGROUND
            else -> return null
        }
        val candidate = Waystone(canonicalX, y, layer, defaultName)
        return world.waystones.firstOrNull { it.id == candidate.id } ?: candidate.also {
            world.waystones = world.waystones + it
        }
    }

    override fun destinations(sourceId: String): List<Waystone> {
        world.waystones = world.waystones.filter { !world.isChunkLoadedAt(it.x) || exists(it) }
        val source = world.waystones.firstOrNull { it.id == sourceId } ?: return emptyList()
        return world.waystones.filter { it.id != sourceId }
            .sortedWith(compareBy<Waystone> { distance(source, it) }.thenBy { it.name }.thenBy { it.id })
    }

    override fun rename(id: String, name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > 32 || trimmed.any { it.isISOControl() }) return false
        val stone = world.waystones.firstOrNull { it.id == id } ?: return false
        if (!exists(stone)) return false
        world.waystones = world.waystones.map { if (it.id == id) it.copy(name = trimmed) else it }
        return true
    }

    override fun distance(source: Waystone, destination: Waystone): Long {
        val difference = abs(source.x.toLong() - destination.x.toLong())
        return if (world.isInfinite) difference else min(difference, world.width.toLong() - difference)
    }

    override fun canUse(sourceId: String): Boolean {
        val source = world.waystones.firstOrNull { it.id == sourceId } ?: return false
        if (player.isDead || player.health <= 0 || player.isInBed || !exists(source)) return false
        var dx = abs(player.position.x - (source.x + .5f))
        if (!world.isInfinite) dx = min(dx, abs(world.width - dx))
        val dy = player.position.y - (source.y + .5f)
        val range = if (player.gameMode.isCreative()) 9f else 6f
        return dx * dx + dy * dy <= range * range
    }

    override fun teleport(sourceId: String, destinationId: String): WaystoneTravelResult {
        if (!canUse(sourceId) || world.world.isLocked) return WaystoneTravelResult.SOURCE_UNAVAILABLE
        val destination = world.waystones.firstOrNull { it.id == destinationId && it.id != sourceId }
            ?: return WaystoneTravelResult.DESTINATION_UNAVAILABLE
        world.ensureTerrainLoaded(destination.x - 3, destination.x + 3)
        if (!exists(destination)) {
            remove(destination.id)
            return WaystoneTravelResult.DESTINATION_UNAVAILABLE
        }
        // Readiness callbacks restore saved mobs and vehicles; validate occupancy after those callbacks.
        world.prepareArrival(destination.x - 3, destination.x + 3)
        mobs.flushPendingSpawns()
        val arrival = WaystoneArrival.find(
            x = destination.x,
            y = destination.y,
            width = player.width,
            height = player.height,
            canonicalX = world::containerX,
            safe = { vehicles.clear(it, ignorePlayer = true) && harmless(it) },
            supported = { bounds ->
                var supported = false
                forEachBlockInArea(bounds) { x, y ->
                    val block = world.getForeMap(x, y)
                    if (block.params.hasCollision && block.getRectangle(x, y).overlaps(bounds)) supported = true
                }
                supported && harmless(bounds)
            },
        ) ?: return WaystoneTravelResult.DESTINATION_BLOCKED

        vehicles.resetMovement()
        player.teleportTo(arrival.x, arrival.y)
        // The next physics tick streams from the new view, even before the renderer consumes the camera reset.
        context.getCameraContext().visibleWorld.setCenter(arrival.x, arrival.y)
        world.onPlayerTeleported()
        return WaystoneTravelResult.SUCCESS
    }

    private fun harmless(bounds: Rectangle): Boolean {
        var safe = true
        forEachBlockInArea(bounds) { x, y ->
            for (block in listOf(world.getForeMap(x, y), world.getBackMap(x, y))) {
                if ((block.isFluid() || block.isFire() || block.params.damage > 0) && block.getRectangle(x, y).overlaps(bounds)) safe = false
            }
        }
        return safe
    }

    private fun exists(stone: Waystone): Boolean = stone.y in 0 until world.height &&
        (if (stone.layer == Layer.FOREGROUND) world.getForeMap(stone.x, stone.y) else world.getBackMap(stone.x, stone.y)).params.key == KEY

    private fun remove(id: String) {
        world.waystones = world.waystones.filterNot { it.id == id }
    }

    override fun onBlockDestroyed(block: Block, x: Int, y: Int, layer: Layer, withDrop: Boolean, destroyedByPlayer: Boolean) {
        if (block.params.key == KEY) remove(Waystone(world.containerX(x), y, layer, "").id)
    }

    companion object {
        private const val KEY = "waystone"
    }
}
