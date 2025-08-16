package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryWindow
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.isInsideWindow
import javax.inject.Inject
import kotlin.math.abs

@GameScope
@BindMouseInputHandler
class CreativeInventoryScrollMouseInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val itemsRepository: ItemsRepository,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])

    private var dragStartY = 0f

    override fun checkConditions(action: MouseInputAction): Boolean = gameWindowsManager.currentWindowType == GameWindowType.CREATIVE_INVENTORY &&
        (gameWindowsManager.isDragging || isInsideWindow(action, creativeInventoryTexture)) &&
        (
            checkStartDragConditions(action) ||
                checkEndDragConditions(action) ||
                checkDragConditions(action) ||
                action.actionKey is MouseInputActionKey.Scroll
            )

    private fun checkStartDragConditions(action: MouseInputAction): Boolean = (action.actionKey is MouseInputActionKey.Screen) &&
        !action.actionKey.touchUp &&
        !gameWindowsManager.isDragging

    private fun checkEndDragConditions(action: MouseInputAction): Boolean = action.actionKey is MouseInputActionKey.Screen &&
        action.actionKey.touchUp &&
        gameWindowsManager.isDragging

    private fun checkDragConditions(action: MouseInputAction): Boolean = applicationContextRepository.isTouch() &&
        action.actionKey is MouseInputActionKey.Dragged &&
        abs(action.screenY - dragStartY) >= DRAG_SENSITIVITY

    private fun clampScrollAmount() {
        gameWindowsManager.creativeScrollAmount =
            MathUtils.clamp(
                /* value = */ gameWindowsManager.creativeScrollAmount,
                /* min = */ 0,
                /* max = */ (gameWindowsManager.currentWindow as CreativeInventoryWindow).getMaxScroll(itemsRepository),
            )
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
