package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject
import kotlin.math.max

@GameScope
@BindMouseInputHandler
class CloseGameWindowMouseInputHandler @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val mobController: MobController,
    private val dropController: DropController,
    private val textureRegions: GetTextureRegionByNameUseCase,
) : IMouseInputHandler {

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])
    private val survivalInventoryTexture get() = requireNotNull(textureRegions["survival"])
    private val craftingInventoryTexture get() = requireNotNull(textureRegions["crafting_table"])
    private val furnaceInventoryTexture get() = requireNotNull(textureRegions["furnace"])
    private val chestInventoryTexture get() = requireNotNull(textureRegions["chest"])

    override fun checkConditions(action: MouseInputAction): Boolean {
        return gameWindowsManager.currentWindowType != GameWindowType.NONE &&
            (action.actionKey is MouseInputActionKey.Left || action.actionKey is MouseInputActionKey.Screen) &&
            !action.actionKey.touchUp &&
            !getWindowRect(gameContextRepository.getCameraContext().viewport).contains(action.screenX, action.screenY)
    }

    private fun getWindowRect(viewport: Rectangle): Rectangle {
        val windowTexture = getCurrentWindowTexture()

        val window = gameWindowsManager.currentWindow as? AbstractInventoryWindowWithCraftGrid
        val (recipeBookWidth, recipeBookHeight) = textureRegions["recipe_book"]
            ?.takeIf { window?.recipeBookActive == true }
            ?.run { regionWidth.toFloat() to regionHeight.toFloat() }
            ?: (0f to 0f)

        return Rectangle(
            0f,
            0f,
            recipeBookWidth + windowTexture.regionWidth.toFloat(),
            max(windowTexture.regionHeight.toFloat(), recipeBookHeight),
        ).apply {
            if (window?.recipeBookActive != true) {
                setCenter(viewport.getCenter(Vector2()))
            } else {
                x = viewport.width / 2f - recipeBookWidth
                y = viewport.height / 2f - height / 2f
            }
        }
    }

    private fun getCurrentWindowTexture(): TextureRegion = when (val window = gameWindowsManager.currentWindowType) {
        GameWindowType.CREATIVE_INVENTORY -> creativeInventoryTexture
        GameWindowType.SURVIVAL_INVENTORY -> survivalInventoryTexture
        GameWindowType.CRAFTING_TABLE -> craftingInventoryTexture
        GameWindowType.FURNACE -> furnaceInventoryTexture
        GameWindowType.CHEST -> chestInventoryTexture
        else -> throw UnsupportedOperationException("Cant close window ${window.name}")
    }

    override fun handle(action: MouseInputAction) {
        val selectedItem = gameWindowsManager.currentWindow?.selectedItem
        if (selectedItem != null) {
            dropController.addDrop(
                x = mobController.player.position.x + (2f * mobController.player.direction.basis),
                y = mobController.player.position.y,
                item = selectedItem.item,
                count = selectedItem.amount,
                durability = selectedItem.durability,
            )
            gameWindowsManager.currentWindow?.selectedItem = null
        }
        gameWindowsManager.closeWindow()
    }
}
