package ru.deadsoftware.cavedroid.game.world

import com.badlogic.gdx.utils.Timer
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.misc.utils.forEachBlockInArea
import javax.inject.Inject
import kotlin.math.max

@GameScope
class GameWorldMobDamageControllerTask @Inject constructor(
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
    private val gameItemsHolder: GameItemsHolder,
) : Timer.Task() {

    override fun run() {
        sequence {
            yield(mobsController.player)
            yieldAll(mobsController.mobs)
        }.forEach { mob ->
            forEachBlockInArea(mob) { x, y ->
                val foregroundBlock = gameWorld.getForeMap(x, y)
                val backgroundBlock = gameWorld.getBackMap(x, y)

                mob.damage(max(foregroundBlock.params.damage, backgroundBlock.params.damage))
            }
        }


    }

    companion object {
        const val ENVIRONMENTAL_MOB_DAMAGE_INTERVAL_SEC = 0.5f
    }

}