package ru.fredboy.cavedroid.gameplay.controls.input.handler.keyboard

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.KeyboardInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.KeyboardInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindKeyboardInputHandler
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class PauseGameKeyboardInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameController: ApplicationController,
    private val dropController: DropController,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val containerController: ContainerController,
    private val gameWindowsManager: GameWindowsManager,
    private val saveDataRepository: SaveDataRepository,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean = action.actionKey is KeyboardInputActionKey.Pause && action.isKeyDown

    override fun handle(action: KeyboardInputAction) {
        if (gameWindowsManager.currentWindowType != GameWindowType.NONE) {
            gameWindowsManager.closeWindow()
            return
        }

        saveDataRepository.save(
            gameDataFolder = applicationContextRepository.getGameDirectory(),
            dropController = dropController,
            mobController = mobController,
            containerController = containerController,
            gameWorld = gameWorld,
        )

        gameController.quitGame()
    }
}
