package ru.fredboy.cavedroid.ux.physics.task

import com.badlogic.gdx.utils.Timer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.math.max

@GameScope
class GameWorldMobDamageControllerTask @Inject constructor(
    private val mobController: MobController,
    private val gameWorld: GameWorld,
) : Timer.Task() {

    override fun run() {
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
        }
    }

    companion object {
        const val ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC = 0.5f
    }
}
