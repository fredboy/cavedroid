package ru.fredboy.cavedroid.game.controller.mob.factory

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.entity.mob.model.Pig
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.behavior.PigMobBehavior
import javax.inject.Inject

@GameScope
class PigFactory @Inject constructor(
    private val mobController: MobController,
    private val gameAssetsHolder: GameAssetsHolder,
) {

    fun create(x: Float, y: Float): Pig {
        val pig = Pig(
            x = x,
            y = y,
            behavior = PigMobBehavior(),
            sprite = gameAssetsHolder.getPigSprites(),
        )

        mobController.addMob(pig)

        return pig
    }
}
