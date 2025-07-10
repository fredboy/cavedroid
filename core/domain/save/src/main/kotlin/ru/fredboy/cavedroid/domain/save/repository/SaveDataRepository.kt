package ru.fredboy.cavedroid.domain.save.repository

import ru.fredboy.cavedroid.domain.save.model.GameMapSaveData
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
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
        gameWorld: GameWorld,
    )

    fun loadMap(
        gameDataFolder: String,
    ): GameMapSaveData

    fun loadContainerController(
        gameDataFolder: String,
        containerWorldAdapter: ContainerWorldAdapter,
        containerFactory: ContainerFactory,
        dropAdapter: DropAdapter,
    ): ContainerController

    fun loadDropController(
        gameDataFolder: String,
        dropWorldAdapter: DropWorldAdapter,
        dropQueue: DropQueue,
    ): DropController

    fun loadMobController(
        gameDataFolder: String,
        mobWorldAdapter: MobWorldAdapter,
        mobPhysicsFactory: MobPhysicsFactory,
        dropQueue: DropQueue,
    ): MobController

    fun exists(gameDataFolder: String): Boolean
}
