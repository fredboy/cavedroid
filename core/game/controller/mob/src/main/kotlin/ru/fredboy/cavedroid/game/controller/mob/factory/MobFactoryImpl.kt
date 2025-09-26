package ru.fredboy.cavedroid.game.controller.mob.factory

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.repository.MobParamsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.MobQueue
import ru.fredboy.cavedroid.entity.mob.abstraction.MobFactory
import ru.fredboy.cavedroid.entity.mob.model.ArcherMob
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.SheepMob
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob
import javax.inject.Inject

@GameScope
class MobFactoryImpl @Inject constructor(
    private val mobParamsRepository: MobParamsRepository,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val mobQueue: MobQueue,
) : MobFactory {

    override fun create(x: Float, y: Float, mobKey: String): Mob? {
        val mobParams = mobParamsRepository.getMobParamsByKey(mobKey) ?: run {
            logger.e { "No mob params found for $mobKey" }
            return null
        }

        return when (mobParams.behaviorType) {
            MobBehaviorType.PASSIVE, MobBehaviorType.AGGRESSIVE -> WalkingMob(
                params = mobParams,
            )

            MobBehaviorType.SHEEP -> SheepMob(mobParams)

            MobBehaviorType.ARCHER -> ArcherMob(getItemByKeyUseCase, mobParams)

            else -> run {
                logger.w { "Mobs of type ${mobParams.behaviorType} not yet supported" }
                null
            }
        }?.also { mob ->
            mobQueue.offerMob(x, y, mob)
        }
    }

    companion object {
        private const val TAG = "MobFactory"
private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
