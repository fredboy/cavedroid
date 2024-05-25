package ru.deadsoftware.cavedroid.game.save

import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.objects.container.ContainerController
import ru.deadsoftware.cavedroid.game.objects.drop.DropController

class GameSaveData(
    private var mobsController: MobsController?,
    private var dropController: DropController?,
    private var containerController: ContainerController?,
    private var foreMap: Array<Array<Block>>?,
    private var backMap: Array<Array<Block>>?
) {

    fun retrieveMobsController(): MobsController {
        val value = requireNotNull(mobsController)
        mobsController = null
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
        return mobsController == null &&
                dropController == null &&
                containerController == null &&
                foreMap == null &&
                backMap == null
    }


}