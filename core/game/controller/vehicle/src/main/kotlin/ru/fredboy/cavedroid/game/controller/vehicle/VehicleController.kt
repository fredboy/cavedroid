package ru.fredboy.cavedroid.game.controller.vehicle

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Filter
import com.badlogic.gdx.physics.box2d.Fixture
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.entity.vehicle.BoatPhysics
import ru.fredboy.cavedroid.entity.vehicle.BoatTuning
import ru.fredboy.cavedroid.entity.vehicle.Vehicle
import ru.fredboy.cavedroid.entity.vehicle.VehicleState
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.abstraction.FixedStepListener
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

@GameScope
class VehicleController @Inject constructor(
    private val world: GameWorld,
    private val mobs: MobController,
    private val factory: BoatPhysicsFactory,
    private val drops: DropQueue,
    private val items: GetItemByKeyUseCase,
    private val sounds: StepsSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) : FixedStepListener {
    private val live = linkedMapOf<String, Vehicle>()
    val vehicles: Collection<Vehicle> get() = live.values
    private val player get() = mobs.player
    val mounted get() = player.mountedVehicleId?.let(live::get)
    private val riderFilters = mutableMapOf<Fixture, Filter>()
    var onRidingChanged: (() -> Unit)? = null

    init {
        world.addFixedStepListener(this)
        player.onDetachVehicle = { detach() }
    }

    fun restore(states: List<VehicleState>) {
        for (state in states) {
            if (state.id in live ||
                state.type != "oak_boat" ||
                state.health <= 0 ||
                !state.x.isFinite() ||
                !state.y.isFinite() ||
                !state.velocityX.isFinite() ||
                !state.velocityY.isFinite()
            ) {
                continue
            }
            live[state.id] = factory.create(state)
        }
    }

    fun resolvePassenger() {
        val boat = mounted
        player.mountedVehicleId = null
        if (boat != null && !player.isDead) board(boat, restoring = true)
    }

    fun evict(boat: Vehicle) {
        if (boat.occupied) return
        live.remove(boat.id)
        world.world.destroyBody(boat.body)
    }

    fun place(x: Float, y: Float): Boolean {
        val bounds = Rectangle(x - BoatTuning.WIDTH / 2f, y - BoatTuning.HEIGHT / 2f, BoatTuning.WIDTH, BoatTuning.HEIGHT)
        if (!reachable(x, y) || !VehicleSpace.canPlace(clear(bounds), supported(bounds), waterArea(bounds))) return false
        restore(listOf(VehicleState(UUID.randomUUID().toString(), x = x, y = y)))
        player.decreaseCurrentItemCount()
        return true
    }

    fun boardTarget(): Boolean = target()?.let { board(it) } ?: false

    fun attackTarget(): Boolean {
        val boat = target() ?: return false
        val tool = player.activeItem.item as? Item.Tool
        boat.damage(if (player.gameMode.isCreative()) BoatTuning.MAX_HEALTH else (tool?.mobDamageMultiplier ?: 1f).toInt())
        if (boat.health > 0) boat.body.setLinearVelocity(boat.velocity.x, BoatPhysics.hitVerticalVelocity(boat.velocity.y))
        if (boat.health <= 0) boat.dropOnBreak = !player.gameMode.isCreative()
        if (tool != null) player.durateActiveDurable()
        player.addExhaustion(Player.EXHAUSTION_PER_ATTACK)
        player.startHitting(false)
        player.stopHitting()
        return true
    }

    private fun target(): Vehicle? = vehicles.lastOrNull {
        it.health > 0 && it.hitbox.contains(player.cursorX, player.cursorY) && reachable(it.position.x, it.position.y)
    }

    private fun reachable(x: Float, y: Float): Boolean {
        if (player.isDead || player.isDisposed()) return false
        val start = Vector2(player.position.x, player.position.y - player.height / 3f)
        val end = Vector2(x, y)
        val range = if (player.gameMode.isCreative()) 9f else 6f
        if (player.position.dst2(end) > range * range) return false
        if (start.epsilonEquals(end)) return true
        var blocked = false
        world.world.rayCast({ fixture, _, _, _ ->
            if (!fixture.isSensor && fixture.filterData.categoryBits.toInt() and PhysicsConstants.CATEGORY_BLOCK.toInt() != 0) {
                blocked = true
                0f
            } else {
                -1f
            }
        }, start, end)
        return !blocked
    }

    private fun board(boat: Vehicle, restoring: Boolean = false): Boolean {
        if (mounted != null || boat.occupied || player.isDead || player.isInBed) return false
        val seat = Rectangle(boat.position.x - player.width / 2f, boat.position.y + BoatTuning.SEAT_OFFSET_Y - player.height / 2f, player.width, player.height)
        if (!restoring && !clear(seat, boat, ignorePlayer = true)) return false
        resetMovement()
        player.isFlyMode = false
        player.canClimb = false
        player.canSwim = false
        player.climb = false
        player.mountedVehicleId = boat.id
        factory.setOccupied(boat, player.width, player.height, true)
        riderFilters.clear()
        player.body.fixtureList.forEach { fixture ->
            val original = fixture.filterData
            riderFilters[fixture] = Filter().apply {
                categoryBits = original.categoryBits
                maskBits = original.maskBits
                groupIndex = original.groupIndex
            }
            fixture.filterData = Filter().apply {
                categoryBits = original.categoryBits
                maskBits = PhysicsConstants.CATEGORY_PROJECTILE
            }
        }
        player.body.type = BodyDef.BodyType.KinematicBody
        syncPassenger(boat)
        onRidingChanged?.invoke()
        return true
    }

    fun dismount(): Boolean {
        val boat = mounted ?: return false
        val exit = VehicleSpace.findExit(
            boat.position.x,
            boat.position.y,
            boat.facing,
            player.width,
            player.height,
            clear = { clear(it, boat, ignorePlayer = true) && reachable(it.x + it.width / 2f, it.y + it.height / 2f) },
            supported = ::supported,
            water = { waterArea(it) > 0f },
            allowAbove = supported(boat.hitbox) || boat.submerged > 0f,
        ) ?: return false
        detach()
        player.body.setTransform(exit, 0f)
        return true
    }

    fun resetMovement() {
        player.controlVector.setZero()
        player.isSprinting = false
        player.tryClimb = false
        player.descend = false
        mounted?.rowing = 0f
    }

    private fun detach() {
        if (world.world.isLocked) return
        val boat = mounted
        player.mountedVehicleId = null
        if (!player.isDisposed()) {
            player.body.type = BodyDef.BodyType.DynamicBody
            player.body.gravityScale = 1f
            player.body.linearDamping = 0f
            riderFilters.forEach { (fixture, filter) -> fixture.filterData = filter }
            if (boat != null) player.body.linearVelocity = boat.velocity
        }
        riderFilters.clear()
        if (boat != null) factory.setOccupied(boat, player.width, player.height, false)
        resetMovement()
        onRidingChanged?.invoke()
    }

    private fun syncPassenger(boat: Vehicle) {
        if (!boat.occupied || player.isDisposed()) return
        player.body.setTransform(boat.position.x, boat.position.y + BoatTuning.SEAT_OFFSET_Y, 0f)
        player.body.setLinearVelocity(0f, 0f)
        player.body.gravityScale = 0f
        player.updateSight()
    }

    fun clear(bounds: Rectangle, ignored: Vehicle? = null, ignorePlayer: Boolean = false): Boolean {
        if (bounds.y < 0 || bounds.y + bounds.height > world.height) return false
        var clear = true
        forEachBlockInArea(bounds) { x, y ->
            if (!world.isChunkLoadedAt(x)) clear = false
            val block = world.getForeMap(x, y)
            if (block.params.hasCollision && block.getRectangle(x, y).overlaps(bounds)) clear = false
        }
        val width = if (world.isInfinite) null else world.width.toFloat()
        return clear &&
            vehicles.none { it !== ignored && VehicleSpace.overlaps(bounds, it.hitbox, width) } &&
            mobs.mobs.none { !it.isDead && VehicleSpace.overlaps(bounds, it.hitbox, width) } &&
            (ignorePlayer || !VehicleSpace.overlaps(bounds, player.hitbox, width))
    }

    private fun supported(bounds: Rectangle): Boolean {
        val feet = Rectangle(bounds.x, bounds.y + bounds.height, bounds.width, 0.15f)
        var result = false
        forEachBlockInArea(feet) { x, y ->
            val block = world.getForeMap(x, y)
            if (block.params.hasCollision && block.getRectangle(x, y).overlaps(feet)) result = true
        }
        return result
    }

    private fun waterArea(bounds: Rectangle): Float {
        var area = 0f
        forEachBlockInArea(bounds) { x, y ->
            val block = world.getForeMap(x, y)
            if (block.isWater()) area += BoatPhysics.overlapArea(bounds, block.getRectangle(x, y))
        }
        return area
    }

    override fun beforeStreaming() {
        val boat = mounted
        val columns = if (boat != null && boat.occupied) {
            val chunk = Math.floorDiv(kotlin.math.floor(boat.position.x).toInt(), ru.fredboy.cavedroid.game.world.generator.ChunkGenerator.CHUNK_W)
            setOf(chunk - 1, chunk, chunk + 1)
        } else {
            emptySet()
        }
        world.retainColumns(columns)
    }

    override fun beforeStep(delta: Float) {
        if (player.isDead || player.isDisposed()) {
            if (mounted != null) detach()
        }
        vehicles.toList().forEach { boat ->
            if (boat.health <= 0 || boat.position.y > world.height) {
                if (boat.occupied) detach()
                if (boat.dropOnBreak) drops.offerItem(boat.position.x, boat.position.y, items[boat.type].toInventoryItem())
                evict(boat)
                return@forEach
            }
            val wasInWater = boat.submerged > 0f
            boat.soundTimer += delta
            boat.submerged = (waterArea(boat.hitbox) / (BoatTuning.WIDTH * BoatTuning.HEIGHT)).coerceIn(0f, 1f)
            boat.rowing = if (boat.occupied) (player.controlVector.x / player.speed).coerceIn(-1f, 1f) else 0f
            if (boat.rowing != 0f) boat.facing = if (boat.rowing < 0) -1 else 1
            if (boat.submerged > 0f) {
                boat.body.setLinearVelocity(BoatPhysics.horizontalVelocity(boat.velocity.x, boat.rowing, boat.submerged, delta), boat.velocity.y)
                boat.body.applyForceToCenter(0f, BoatPhysics.verticalAcceleration(boat.submerged, boat.velocity.y, world.world.gravity.y) * boat.body.mass, true)
                boat.oarPhase += abs(boat.rowing) * delta * 7f
                if (!player.isDisposed() && (!wasInWater || boat.rowing != 0f) && boat.soundTimer >= 0.8f) {
                    boat.soundTimer = 0f
                    sounds.getStepSound("water")?.let { sound ->
                        soundPlayer.playSoundAtPosition(sound, boat.position.x, boat.position.y, player.position.x, player.position.y)
                    }
                }
            }
            syncPassenger(boat)
        }
    }

    override fun afterStep(delta: Float) {
        vehicles.forEach { boat ->
            if (!world.isInfinite) {
                val width = world.width.toFloat()
                val wrapped = when {
                    boat.position.x < 0f -> boat.position.x + width
                    boat.position.x >= width -> boat.position.x - width
                    else -> boat.position.x
                }
                val shift = wrapped - boat.position.x
                if (shift != 0f) {
                    boat.body.setTransform(wrapped, boat.position.y, 0f)
                    if (boat.occupied) {
                        player.aimX += shift
                        player.cursorX += shift
                    }
                }
            }
            syncPassenger(boat)
        }
    }

    /** Called on the same half-second cadence as mob environmental damage. */
    fun environmentalDamage(hasFire: (Int, Int) -> Boolean) {
        vehicles.forEach { boat ->
            var water = false
            var lava = false
            var fire = false
            forEachBlockInArea(boat.hitbox) { x, y ->
                val block = world.getForeMap(x, y)
                if (block.getRectangle(x, y).overlaps(boat.hitbox)) {
                    water = water || block.isWater()
                    lava = lava || block.isLava()
                    fire = fire || block.isFire() || hasFire(x, y)
                }
            }
            if (water) {
                boat.fireTime = 0f
                boat.fireDamageTimer = 0f
            } else {
                if (lava) {
                    boat.damage(4)
                    boat.fireTime = 15f
                } else if (fire) {
                    boat.fireTime = 8f
                }
                if (boat.fireTime > 0f) {
                    boat.fireTime -= 0.5f
                    boat.fireDamageTimer += 0.5f
                    if (boat.fireDamageTimer >= 1f) {
                        boat.damage(1)
                        boat.fireDamageTimer -= 1f
                    }
                }
            }
        }
    }

    override fun dispose() {
        detach()
        vehicles.toList().forEach(::evict)
        player.onDetachVehicle = null
        onRidingChanged = null
    }
}
