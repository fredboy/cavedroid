package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindKeyboardInputHandler
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindKeyboardInputHandler
class PauseGameKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val dropController: DropController,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val containerController: ContainerController,
    private val gameWindowsManager: GameWindowsManager,
    private val saveDataRepository: SaveDataRepository,
) : IKeyboardInputHandler {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Pause && action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        if (gameWindowsManager.getCurrentWindow() != GameUiWindow.NONE) {
            gameWindowsManager.closeWindow()
            return
        }

        saveDataRepository.save(mainConfig.gameFolder, dropController, mobController, containerController, gameWorld)
        mainConfig.caveGame.quitGame()
    }
}