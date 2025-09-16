package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.GameWorldLightManager
import javax.inject.Inject
import kotlin.math.max

@GameScope
class GameWorldMobDamageControllerTask @Inject constructor(
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val gameWorldLightManager: GameWorldLightManager,
) : BaseGameWorldControllerTask() {

    override fun exec() {
        sequence {
            yield(mobController.player)
            yieldAll(mobController.mobs)
        }.forEach { mob ->
            forEachBlockInArea(mob.hitbox) { x, y ->
                val hitbox = mob.hitbox
                val foregroundBlock = gameWorld.getForeMap(x, y).takeIf { it.getSpriteRectangle(x, y).overlaps(hitbox) }
                val backgroundBlock = gameWorld.getBackMap(x, y).takeIf { it.getSpriteRectangle(x, y).overlaps(hitbox) }

                val damage = max(foregroundBlock?.params?.damage ?: 0, backgroundBlock?.params?.damage ?: 0)
                if (damage > 0) {
                    mob.damage(damage)
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

            if (mob.params.takesSunDamage && gameWorld.isDayTime() && gameWorldLightManager.isMobExposedToSun(mob)) {
                mob.damage(SUN_DAMAGE)
            }
        }
    }

    private fun Mob.isHeadInsideSolidBlock(gameWorld: GameWorld): Boolean {
        val x = (position.x + direction.basis * (width / 2f - 0.125f)).toInt()
        val y = (position.y - height / 2f + 0.125f).toInt()

        return gameWorld.getForeMap(x, y).let { block ->
            block.params.hasCollision && block.getRectangle(x, y).overlaps(hitbox)
        }
    }

    companion object {
        private const val SUN_DAMAGE = 4
        const val ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC = 0.5f
    }
}
