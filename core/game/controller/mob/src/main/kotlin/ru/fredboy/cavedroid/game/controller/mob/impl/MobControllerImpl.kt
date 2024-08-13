package ru.fredboy.cavedroid.game.controller.mob.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.model.Player
import java.util.LinkedList
import javax.inject.Inject

@GameScope
class MobControllerImpl @Inject constructor(
    mobAssetsRepository: MobAssetsRepository,
    getFallbackItemUseCase: GetFallbackItemUseCase,
) : MobController {

    private val _mobs = LinkedList<Mob>()

    override val mobs: List<Mob> get() = _mobs

    override var player = Player(mobAssetsRepository.getPlayerSprites(), getFallbackItemUseCase, 0f, 0f)

    override fun addMob(mob: Mob) {
        // TODO: Probably shouldn't add if already in the list
        _mobs.add(mob)
    }

    override fun removeMob(mob: Mob) {
        _mobs.remove(mob)
    }
}