package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.floorToInt
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import javax.inject.Inject
import kotlin.math.max

@GameScope
class GameWorldMobDamageControllerTask @Inject constructor(
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val lightingSystem: LightingSystem,
    private val fireController: FireController,
) : BaseGameWorldControllerTask() {

    override fun exec() {
        sequence {
            yield(mobController.player)
            yieldAll(mobController.mobs)
        }.forEach { mob ->
            var touchingWater = false
            var touchingLava = false
            var touchingFire = false

            forEachBlockInArea(mob.hitbox) { x, y ->
                val hitbox = mob.hitbox
                val foregroundBlock = gameWorld.getForeMap(x, y).takeIf { it.getSpriteRectangle(x, y).overlaps(hitbox) }
                val backgroundBlock = gameWorld.getBackMap(x, y).takeIf { it.getSpriteRectangle(x, y).overlaps(hitbox) }
                val hasFire = fireController.hasFireAt(x, y, Layer.FOREGROUND) ||
                    fireController.hasFireAt(x, y, Layer.BACKGROUND)

                val damage = max(foregroundBlock?.params?.damage ?: 0, backgroundBlock?.params?.damage ?: 0)
                if (damage > 0) {
                    mob.damage(damage)
                }

                if (foregroundBlock?.isWater() == true) touchingWater = true
                if (foregroundBlock?.isLava() == true) touchingLava = true
                if (foregroundBlock?.isFire() == true || backgroundBlock?.isFire() == true || hasFire) {
                    touchingFire = true
                }
            }

            val isUnderWater = gameWorld.getForeMap(mob.mapX, mob.upperMapY)
                .let { it.isWater() && it.getRectangle(mob.mapX, mob.upperMapY).overlaps(mob.hitbox) }

            if (isUnderWater) {
                mob.reduceBreath()
            } else {
                mob.restoreBreath()
            }

            if (mob.isHeadInsideSolidBlock(gameWorld)) {
                mob.damage(4)
            }

            if (mob.behavior.attacksWhenPossible && mob.hitbox.overlaps(mobController.player.hitbox)) {
                mobController.player.damage(mob.params.damageToPlayer)
            }

            val exposedToSun = mob.params.takesSunDamage &&
                gameWorld.isDayTime() &&
                lightingSystem.isMobExposedToSun(mob)

            if (touchingWater) {
                mob.extinguish()
            } else {
                if (touchingLava) {
                    mob.ignite(LAVA_FIRE_DURATION_SEC)
                }
                if (touchingFire) {
                    mob.ignite(FIRE_BLOCK_FIRE_DURATION_SEC)
                }
                if (exposedToSun) {
                    mob.ignite(SUN_FIRE_DURATION_SEC)
                }
            }

            if (mob.isOnFire) {
                mob.fireDamageAccumulator += ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC
                if (mob.fireDamageAccumulator >= FIRE_DAMAGE_INTERVAL_SEC) {
                    mob.fireDamageAccumulator -= FIRE_DAMAGE_INTERVAL_SEC
                    mob.damage(FIRE_DAMAGE)
                }
                mob.decreaseFireTicks(ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC)
            }
        }
    }

    private fun Mob.isHeadInsideSolidBlock(gameWorld: GameWorld): Boolean {
        val x = (position.x + direction.basis * (width / 2f - 0.125f)).floorToInt()
        val y = (position.y - height / 2f + 0.125f).floorToInt()

        return gameWorld.getForeMap(x, y).let { block ->
            block.params.hasCollision && block.params.isFullBlock && block.getRectangle(x, y).overlaps(hitbox)
        }
    }

    companion object {
        private const val FIRE_DAMAGE = 1
        private const val FIRE_DAMAGE_INTERVAL_SEC = 1f
        private const val LAVA_FIRE_DURATION_SEC = 15f
        private const val FIRE_BLOCK_FIRE_DURATION_SEC = 8f
        private const val SUN_FIRE_DURATION_SEC = 1f
        const val ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC = 0.5f
    }
}
