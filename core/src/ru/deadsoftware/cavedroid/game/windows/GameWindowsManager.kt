package ru.deadsoftware.cavedroid.game.windows

import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import javax.inject.Inject

@GameScope
class GameWindowsManager @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
) {

    var creativeScrollAmount = 0
    var isDragging = false

    fun getCurrentWindow(): GameUiWindow {
        return mainConfig.gameUiWindow
    }

    fun openInventory() {
        if (mobsController.player.gameMode == 1) {
            mainConfig.gameUiWindow = GameUiWindow.CREATIVE_INVENTORY
        } else {
            mainConfig.gameUiWindow = GameUiWindow.SURVIVAL_INVENTORY
        }
    }

    fun closeWindow() {
        mainConfig.gameUiWindow = GameUiWindow.NONE
    }

}