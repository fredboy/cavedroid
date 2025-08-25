package ru.fredboy.cavedroid.game.controller.mob.factory

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
class MobFactory @Inject constructor(
    private val mobController: MobController,
    private val mobParamsRepository: MobParamsRepository,
    private val mobPhysicsFactory: MobPhysicsFactory,
) {

    fun create(x: Float, y: Float, mobKey: String): Mob? {
        val mobParams = mobParamsRepository.getMobParamsByKey(mobKey) ?: run {
            Gdx.app.error(TAG, "No mob params found for $mobKey")
            return null
        }

        return when (mobParams.behaviorType) {
            MobBehaviorType.PASSIVE, MobBehaviorType.AGGRESSIVE -> WalkingMob(
                params = mobParams,
            )

            else -> run {
                Gdx.app.log(TAG, "Mobs of type ${mobParams.behaviorType} not yet supported")
                null
            }
        }?.also { mob ->
            mob.spawn(x + 0.5f, y - mobParams.height, mobPhysicsFactory)
            mobController.addMob(mob)
        }
    }

    companion object {
        private const val TAG = "MobFactory"
    }
}
