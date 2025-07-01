package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController

class GameSaveData(
    private var mobController: MobController?,
    private var dropController: DropController?,
    private var containerController: ContainerController?,
) {

    fun retrieveMobsController(): MobController {
        val value = requireNotNull(mobController)
        mobController = null
        return value
    }

    fun retrieveDropController(): DropController {
        val value = requireNotNull(dropController)
        dropController = null
        return value
    }

    fun retrieveContainerController(): ContainerController {
        val value = requireNotNull(containerController)
        containerController = null
        return value
    }

    fun isEmpty(): Boolean = mobController == null &&
        dropController == null &&
        containerController == null
}
