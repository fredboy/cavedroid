package ru.fredboy.cavedroid.game.controller.mob.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
class PlayerAdapterImpl @Inject constructor(
    private val mobController: MobController,
) : PlayerAdapter {

    private val player get() = mobController.player

    override val x: Float
        get() = player.x

    override val y: Float
        get() = player.y

    override val width: Float
        get() = player.width

    override val height: Float
        get() = player.height
}