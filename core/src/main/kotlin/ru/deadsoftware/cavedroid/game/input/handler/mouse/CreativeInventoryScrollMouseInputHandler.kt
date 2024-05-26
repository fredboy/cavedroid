package ru.deadsoftware.cavedroid.game.input.handler.mouse

import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindMouseInputHandler
import com.badlogic.gdx.math.MathUtils
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.input.isInsideWindow
import ru.deadsoftware.cavedroid.misc.Assets
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import javax.inject.Inject
import kotlin.math.abs

@GameScope
@BindMouseInputHandler
class CreativeInventoryScrollMouseInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val gameWindowsManager: GameWindowsManager,
    private val gameItemsHolder: GameItemsHolder,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])

    private var dragStartY = 0f

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.getCurrentWindow() == GameUiWindow.CREATIVE_INVENTORY &&
                (gameWindowsManager.isDragging || isInsideWindow(action, creativeInventoryTexture)) &&
                (checkStartDragConditions(action) || checkEndDragConditions(action) ||
                        checkDragConditions(action) || action.actionKey is MouseInputActionKey.Scroll)

    }

    private fun checkStartDragConditions(action: MouseInputAction): Boolean {
        return (action.actionKey is MouseInputActionKey.Screen) &&
                !action.actionKey.touchUp && !gameWindowsManager.isDragging
    }

    private fun checkEndDragConditions(action: MouseInputAction): Boolean {
        return action.actionKey is MouseInputActionKey.Screen &&
                action.actionKey.touchUp && gameWindowsManager.isDragging
    }

    private fun checkDragConditions(action: MouseInputAction): Boolean {
        return mainConfig.isTouch && action.actionKey is MouseInputActionKey.Dragged &&
                abs(action.screenY - dragStartY) >= DRAG_SENSITIVITY
    }

    private fun clampScrollAmount() {
        gameWindowsManager.creativeScrollAmount =
            MathUtils.clamp(gameWindowsManager.creativeScrollAmount, 0, gameItemsHolder.getMaxCreativeScrollAmount())
    }

    private fun handleStartOrEndDrag(action: MouseInputAction) {
        if (gameWindowsManager.isDragging) {
            gameWindowsManager.isDragging = false
        } else {
            dragStartY = action.screenY
        }
    }

    private fun handleDrag(action: MouseInputAction) {
        gameWindowsManager.isDragging = true
        gameWindowsManager.creativeScrollAmount += ((dragStartY - action.screenY) / DRAG_SENSITIVITY).toInt()
        clampScrollAmount()
        dragStartY = action.screenY
    }

    private fun handleScroll(action: MouseInputAction) {
        gameWindowsManager.creativeScrollAmount += (action.actionKey as MouseInputActionKey.Scroll).amountY.toInt()
        clampScrollAmount()
    }

    override fun handle(action: MouseInputAction) {
        when (action.actionKey) {
            is MouseInputActionKey.Screen -> handleStartOrEndDrag(action)
            is MouseInputActionKey.Dragged -> handleDrag(action)
            is MouseInputActionKey.Scroll -> handleScroll(action)
            else -> return
        }
    }

    companion object {
        private const val DRAG_SENSITIVITY = 16f
    }
}