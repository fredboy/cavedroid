package ru.fredboy.cavedroid.domain.save.repository

import ru.fredboy.cavedroid.domain.save.model.GameSaveData
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

interface SaveDataRepository {

    fun save(
        gameDataFolder: String,
        dropController: DropController,
        mobController: MobController,
        containerController: ContainerController,
        gameWorld: GameWorld
    )

    fun load(
        gameDataFolder: String,
    ): GameSaveData

    fun exists(gameDataFolder: String): Boolean

}