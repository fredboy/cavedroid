package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.VillagerMob
import ru.fredboy.cavedroid.entity.mob.model.VillagerMob.BlockPosition
import kotlin.math.abs

class VillagerMobBehavior : BaseMobBehavior<VillagerMob>(VillagerMob::class) {
    private var targetX: Float? = null
    private var idleSeconds = MathUtils.random(1f, 4f)
    private var stuckSeconds = 0f
    private var previousX = Float.NaN
    private var spawnX: Float? = null

    override fun VillagerMob.updateMob(
        worldAdapter: MobWorldAdapter,
        playerAdapter: PlayerAdapter,
        projectileAdapter: ProjectileAdapter,
        delta: Float,
    ) {
        closeDoors(worldAdapter, playerAdapter)
        val bed = home
        if (bed != null) {
            // A home can be just beyond the streamed region while its owner is still resident.
            if (!worldAdapter.isChunkLoadedAt(bed.x) || !worldAdapter.isChunkLoadedAt(bed.x + 1)) {
                controlVector.setZero()
                isSleeping = false
                return
            }
            if (worldAdapter.getForegroundBlock(bed.x, bed.y).params.key != "bed_l" ||
                worldAdapter.getForegroundBlock(bed.x + 1, bed.y).params.key != "bed_r"
            ) {
                isDead = true
                controlVector.setZero()
                return
            }
        }

        val center = bed?.let { nearestX(it.x + 1f, worldAdapter) } ?: (spawnX ?: position.x.also { spawnX = it })
        val goingHome = bed != null && !worldAdapter.isDayTime()
        if (bed != null &&
            goingHome &&
            abs(position.x - center) < 0.35f &&
            abs(position.y + height / 2f - (bed.y + 1f)) < 0.2f &&
            footContactCounter > 0 &&
            !takingDamage
        ) {
            isSleeping = true
            controlVector.setZero()
            velocity.set(0f, 0f)
            body.setTransform(center, bed.y + 1f - height / 2f, 0f)
            anim = 0f
            targetX = null
            return
        }
        isSleeping = false
        if (goingHome || abs(position.x - center) > WANDER_RADIUS) {
            targetX = center
        } else if (targetX == null) {
            idleSeconds -= delta
            if (idleSeconds <= 0f) targetX = center + MathUtils.random(-WANDER_RADIUS, WANDER_RADIUS)
        }

        val target = targetX
        if (target == null) {
            controlVector.setZero()
            return
        }
        if (abs(target - position.x) < 0.25f) {
            stopWalking()
            return
        }
        direction = if (target > position.x) Direction.RIGHT else Direction.LEFT
        controlVector.set(direction.basis * speed, 0f)
        val aheadX = MathUtils.floor(position.x + direction.basis * 0.8f)
        if (!worldAdapter.isChunkLoadedAt(aheadX) || !hasSafeGround(worldAdapter, aheadX)) {
            stopWalking()
            return
        }
        openDoors(worldAdapter, playerAdapter)
        if (checkForAutojump(worldAdapter)) jump()
        if (canSwim) climbUp() else climb = false

        stuckSeconds = if (abs(position.x - previousX) < delta * speed * 0.1f) stuckSeconds + delta else 0f
        previousX = position.x
        if (stuckSeconds > 2f) stopWalking()
    }

    private fun VillagerMob.stopWalking() {
        targetX = null
        controlVector.setZero()
        idleSeconds = MathUtils.random(2f, 5f)
        stuckSeconds = 0f
    }

    private fun VillagerMob.hasSafeGround(world: MobWorldAdapter, x: Int): Boolean {
        // Allow village steps and shallow troughs, but do not wander off cliffs or into lava.
        // Box2D leaves feet slightly above the supporting tile; include that collision skin.
        val groundY = MathUtils.floor(position.y + height / 2f + 0.05f)
        for (y in groundY - 1..groundY + 1) {
            val block = world.getForegroundBlock(x, y)
            if (block.isLava()) return false
            if (block.params.hasCollision && !block.params.key.startsWith("door_")) return true
        }
        return canSwim
    }

    private fun VillagerMob.openDoors(world: MobWorldAdapter, player: PlayerAdapter) {
        for (x in MathUtils.floor(position.x - 1f)..MathUtils.floor(position.x + 1f)) {
            for (y in upperMapY..lowerMapY) {
                val block = world.getForegroundBlock(x, y)
                val key = block.params.key
                if (key.startsWith("door_wood_bottom_") && key.endsWith("_closed") && world.setWoodenDoorOpen(x, y, true)) {
                    openedDoors.add(BlockPosition(x, y))
                    world.playBlockActionSound(block, x, y, player)
                }
            }
        }
    }

    private fun VillagerMob.closeDoors(world: MobWorldAdapter, player: PlayerAdapter) {
        openedDoors.removeAll { door ->
            if (!world.isChunkLoadedAt(door.x)) return@removeAll false
            val block = world.getForegroundBlock(door.x, door.y)
            if (!block.params.key.endsWith("_open")) return@removeAll true
            val closed = abs(position.x - nearestX(door.x + 0.5f, world)) > 1.5f && world.setWoodenDoorOpen(door.x, door.y, false)
            if (closed) world.playBlockActionSound(block, door.x, door.y, player)
            closed
        }
    }

    private fun VillagerMob.nearestX(x: Float, world: MobWorldAdapter): Float {
        if (world.isInfinite) return x
        val width = world.width.toFloat()
        return x + MathUtils.round((position.x - x) / width) * width
    }

    companion object {
        const val WANDER_RADIUS = 10f
    }
}
