package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameSaver
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.game.objects.furnace.FurnaceController
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class PauseGameKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val dropController: DropController,
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
    private val furnaceController: FurnaceController,
) : IGameInputHandler<KeyboardInputAction> {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return action.actionKey is KeyboardInputActionKey.Pause && action.isKeyDown
    }

    override fun handle(action: KeyboardInputAction) {
        GameSaver.save(mainConfig, dropController, mobsController, furnaceController, gameWorld)
        mainConfig.caveGame.quitGame()
    }
}