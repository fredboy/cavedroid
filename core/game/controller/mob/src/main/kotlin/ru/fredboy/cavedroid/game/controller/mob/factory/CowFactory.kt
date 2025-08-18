package ru.fredboy.cavedroid.game.controller.mob.factory

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.Cow
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
class CowFactory @Inject constructor(
    private val mobController: MobController,
    private val gameAssetsHolder: GameAssetsHolder,
    private val mobPhysicsFactory: MobPhysicsFactory,
) {

    fun create(x: Float, y: Float): Cow {
        val cow = Cow(
            sprite = gameAssetsHolder.getCowSprites(),
        ).apply { spawn(x, y, mobPhysicsFactory) }

        mobController.addMob(cow)

        return cow
    }
}
