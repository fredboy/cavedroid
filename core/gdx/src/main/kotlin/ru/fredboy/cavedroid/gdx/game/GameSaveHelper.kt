package ru.fredboy.cavedroid.gdx.game

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class GameSaveHelper @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val dropController: DropController,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val containerController: ContainerController,
    private val saveDataRepository: SaveDataRepository,
) {

    fun saveGame(overwrite: Boolean) {
        val actualSaveDir = saveDataRepository.getActualSaveDirName(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            overwrite = overwrite,
        )

        gameContextRepository.setSaveGameDirectory(actualSaveDir)

        saveDataRepository.save(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            saveGameDirectory = gameContextRepository.getSaveGameDirectory(),
            worldName = gameContextRepository.getWorldName(),
            dropController = dropController,
            mobController = mobController,
            containerController = containerController,
            gameWorld = gameWorld,
        )
    }
}
