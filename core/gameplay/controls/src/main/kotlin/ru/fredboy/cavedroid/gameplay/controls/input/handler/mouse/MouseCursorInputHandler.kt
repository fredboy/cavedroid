package ru.fredboy.cavedroid.gameplay.controls.input.handler.mouse

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByIndexUseCase
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.gameplay.controls.input.IMouseInputHandler
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction
import ru.fredboy.cavedroid.gameplay.controls.input.action.keys.MouseInputActionKey
import ru.fredboy.cavedroid.gameplay.controls.input.annotation.BindMouseInputHandler
import javax.inject.Inject

@GameScope
@BindMouseInputHandler
class MouseCursorInputHandler @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val tooltipManager: TooltipManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getItemByIndexUseCase: GetItemByIndexUseCase,
    private val fonts: FontTextureAssetsRepository,
) : IMouseInputHandler {

    private val player get() = mobController.player

    private val creativeInventoryTexture get() = requireNotNull(textureRegions["creative"])

    private fun getPlayerHeadRotation(mouseWorldX: Float, mouseWorldY: Float): Float {
        val h = mouseWorldX - player.position.x
        val v = mouseWorldY - (player.position.y - player.height / 2f + player.width / 2f)
        val rotation = MathUtils.atan(v / h) * MathUtils.radDeg
        return MathUtils.clamp(rotation, -45f, 45f)
    }

    private fun handleMouse(action: MouseInputAction) {
        if (gameWindowsManager.currentWindowType != GameWindowType.NONE) {
            return
        }

        val worldX = action.screenX.meters + gameContextRepository.getCameraContext().visibleWorld.x
        val worldY = action.screenY.meters + gameContextRepository.getCameraContext().visibleWorld.y

        player.aimX = worldX
        player.aimY = worldY

        player.headRotation = getPlayerHeadRotation(worldX, worldY)

        if (worldX < player.position.x) {
            player.direction = Direction.LEFT
        } else if (worldX > player.position.x) {
            player.direction = Direction.RIGHT
        }
    }

    private fun getCreativeTooltip(action: MouseInputAction): String? {
        val creativeTexture = creativeInventoryTexture
        val xOnGrid = (
            action.screenX - (
                gameContextRepository.getCameraContext().viewport.width / 2 - creativeTexture.regionWidth / 2 +
                    GameWindowsConfigs.Creative.itemsGridMarginLeft
                )
            ) /
            GameWindowsConfigs.Creative.itemsGridColWidth
        val yOnGrid = (
            action.screenY - (
                gameContextRepository.getCameraContext().viewport.height / 2 - creativeTexture.regionHeight / 2 +
                    GameWindowsConfigs.Creative.itemsGridMarginTop
                )
            ) /
            GameWindowsConfigs.Creative.itemsGridRowHeight

        if (xOnGrid < 0 ||
            xOnGrid >= GameWindowsConfigs.Creative.itemsInRow ||
            yOnGrid < 0 ||
            yOnGrid >= GameWindowsConfigs.Creative.itemsInCol
        ) {
            return null
        }

        val itemIndex = (
            gameWindowsManager.creativeScrollAmount * GameWindowsConfigs.Creative.itemsInRow +
                (xOnGrid.toInt() + yOnGrid.toInt() * GameWindowsConfigs.Creative.itemsInRow)
            )
        val item = getItemByIndexUseCase[itemIndex]

        return item.params.name
    }

    private fun getCreativeTabTooltip(action: MouseInputAction): String? {
        val window = gameWindowsManager.currentWindow as? CreativeInventoryTabsWindow ?: return null
        val texture = requireNotNull(textureRegions[if (window.selectedTab.isInventory) "creative_inventory_tab" else "creative_items_tab"])
        val tabHeight = requireNotNull(textureRegions["creative_tab_selected"]).regionHeight.toFloat()
        val viewport = gameContextRepository.getCameraContext().viewport
        val x = action.screenX - (viewport.width / 2 - texture.regionWidth / 2)
        val y = action.screenY - (viewport.height / 2 - (texture.regionHeight + tabHeight) / 2)
        val tab = CreativeInventoryTabsWindow.Tab.entries.firstOrNull {
            GameWindowsConfigs.CreativeTabs.tabBounds(it.ordinal, texture.regionWidth.toFloat(), texture.regionHeight.toFloat(), tabHeight).contains(x, y)
        } ?: return null
        return fonts.getMenuLocalizationBundle()[tab.localizationKey]
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        return !applicationContextRepository.isTouch() && action.actionKey is MouseInputActionKey.None
    }

    override fun handle(action: MouseInputAction) {
        val pastSelectedX = player.selectedX
        val pastSelectedY = player.selectedY

        if (!applicationContextRepository.isTouch()) {
            handleMouse(action)
            mobController.rayCastPlayerCursor()
        }

        if (player.selectedX != pastSelectedX || player.selectedY != pastSelectedY) {
            player.blockDamage = 0f
        }

        val tooltip = when (gameWindowsManager.currentWindowType) {
            GameWindowType.CREATIVE_INVENTORY -> getCreativeTooltip(action)
            GameWindowType.CREATIVE_INVENTORY_TABS -> getCreativeTabTooltip(action)
            else -> null
        }
        tooltipManager.showMouseTooltip(tooltip.orEmpty())
    }

    override fun reset() {
        // no-op
    }
}
