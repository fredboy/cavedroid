package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
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

            if (mob.behavior.attacksWhenPossible && mob.hitbox.overlaps(mobController.player.hitbox)) {
                mobController.player.damage(mob.params.damageToPlayer)
            }

            if (mob.params.takesSunDamage && gameWorld.isDayTime() && gameWorldLightManager.isMobExposedToSun(mob)) {
                mob.damage(SUN_DAMAGE)
            }
        }
    }

    companion object {
        private const val SUN_DAMAGE = 4
        const val ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC = 0.5f
    }
}
