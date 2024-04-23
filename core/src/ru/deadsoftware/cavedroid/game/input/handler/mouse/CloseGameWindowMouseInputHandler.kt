package ru.deadsoftware.cavedroid.game.input.handler.mouse

import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class CloseGameWindowMouseInputHandler @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
) : IGameInputHandler<MouseInputAction> {

    private val creativeInventoryTexture get() = requireNotNull(Assets.textureRegions["creative"])
    private val survivalInventoryTexture get() = requireNotNull(Assets.textureRegions["survival"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() != GameUiWindow.NONE &&
                action.actionKey is MouseInputActionKey.Left &&
                !action.actionKey.touchUp &&
                !isInsideWindow(action, getCurrentWindowTexture())
    }

    private fun getCurrentWindowTexture(): TextureRegion {
        return when (val window = gameWindowsManager.getCurrentWindow()) {
            GameUiWindow.CREATIVE_INVENTORY -> creativeInventoryTexture
            GameUiWindow.SURVIVAL_INVENTORY -> survivalInventoryTexture
            else -> throw UnsupportedOperationException("Cant close window ${window.name}")
        }
    }

    override fun handle(action: MouseInputAction) {
        gameWindowsManager.closeWindow()
    }

}