package ru.fredboy.cavedroid.game.controller.mob.factory

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.Pig
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.behavior.PigMobBehavior
import javax.inject.Inject

@GameScope
class PigFactory @Inject constructor(
    private val mobController: MobController,
    private val gameAssetsHolder: GameAssetsHolder,
    private val mobWorldAdapter: MobWorldAdapter,
) {

    fun create(x: Float, y: Float): Pig {
        val pig = Pig(
            behavior = PigMobBehavior(),
            sprite = gameAssetsHolder.getPigSprites(),
        ).apply { spawn(x, y, mobWorldAdapter.getBox2dWorld()) }

        mobController.addMob(pig)

        return pig
    }
}
