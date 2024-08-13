package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController

class GameSaveData(
    private var mobController: MobController?,
    private var dropController: DropController?,
    private var containerController: ContainerController?,
    private var foreMap: Array<Array<Block>>?,
    private var backMap: Array<Array<Block>>?
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

    fun retrieveForeMap(): Array<Array<Block>> {
        val value = requireNotNull(foreMap)
        foreMap = null
        return value
    }

    fun retrieveBackMap(): Array<Array<Block>> {
        val value = requireNotNull(backMap)
        backMap = null
        return value
    }

    fun isEmpty(): Boolean {
        return mobController == null &&
                dropController == null &&
                containerController == null &&
                foreMap == null &&
                backMap == null
    }


}