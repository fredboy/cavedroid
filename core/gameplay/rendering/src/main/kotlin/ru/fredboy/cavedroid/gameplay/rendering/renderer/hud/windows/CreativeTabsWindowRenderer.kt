package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.domain.assets.repository.WearableTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.WindowsRenderer
import javax.inject.Inject
import kotlin.math.min

@GameScope
class CreativeTabsWindowRenderer @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
    private val wearableTextureAssetsRepository: WearableTextureAssetsRepository,
    private val itemsRepository: ItemsRepository,
) : AbstractWindowRenderer(),
    IHudRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val inventoryTabWindowTexture get() = requireNotNull(textureRegions[CREATIVE_INVENTORY_TAB_KEY])
    private val itemsTabWindowTexture get() = requireNotNull(textureRegions[CREATIVE_ITEMS_TAB_KEY])
    private val searchTabWindowTexture get() = requireNotNull(textureRegions[CREATIVE_SEARCH_TAB_KEY])
    private val inventoryCharTexture get() = requireNotNull(textureRegions[INVENTORY_CHAR_KEY])

    private val selectedTabTexture get() = requireNotNull(textureRegions[CREATIVE_TAB_SELECTED_KEY])
    private val unselectedTabTexture get() = requireNotNull(textureRegions[CREATIVE_TAB_UNSELECTED_KEY])
    private val scrollIndicatorTexture get() = requireNotNull(textureRegions[SCROLL_INDICATOR_KEY])

    private class TabInfo(
        val windowTexture: TextureRegion,
        val tabIconSprite: Sprite,
    )

    private val tabs = CreativeInventoryTabsWindow.Tab.entries.associateWith { tab ->
        if (tab.isInventory) {
            return@associateWith TabInfo(inventoryTabWindowTexture, itemsRepository.getItemByKey("chest").sprite)
        }

        TabInfo(
            windowTexture = itemsTabWindowTexture,
            tabIconSprite = itemsRepository.getAllItems().first { tab.isRelevantItem(it) }.sprite,
        )
    }

    private val currentTabInfo: TabInfo
        get() = tabs[(gameWindowsManager.currentWindow as CreativeInventoryTabsWindow).selectedTab]!!

    private fun drawPlayerPortrait(spriteBatch: SpriteBatch, windowX: Float, windowY: Float) {
        val portraitX = windowX + GameWindowsConfigs.CreativeTabs.portraitMarginLeft
        val portraitY = windowY + GameWindowsConfigs.CreativeTabs.portraitMarginTop

        spriteBatch.draw(
            inventoryCharTexture,
            portraitX,
            portraitY,
            GameWindowsConfigs.CreativeTabs.portraitWidth,
            GameWindowsConfigs.CreativeTabs.portraitHeight,
        )

        val armorSprites = mobController.player.wearingArmor.items.mapIndexedNotNull { slot, item ->
            val armorPiece = item.item as? Item.Armor ?: return@mapIndexedNotNull null
            val material = armorPiece.material

            val (armorX, armorY) = when (armorPiece) {
                is Item.Helmet -> GameWindowsConfigs.CreativeTabs.run { helmX to helmY }
                is Item.Chestplate -> GameWindowsConfigs.CreativeTabs.run { chestX to chestY }
                is Item.Leggings -> GameWindowsConfigs.CreativeTabs.run { pantX to pantY }
                is Item.Boots -> GameWindowsConfigs.CreativeTabs.run { bootX to bootY }
            }

            wearableTextureAssetsRepository.getFrontSprite(material, slot)
                ?.apply {
                    setScale(2f / 3f)
                    setPosition(portraitX + armorX, portraitY + armorY)
                    setColor(armorPiece.params.tint ?: Color.WHITE)
                }
        }

        armorSprites.forEach { sprite ->
            sprite.draw(spriteBatch)
        }
    }

    private fun drawItemTabContent(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
    ) {
        val windowTexture = currentTabInfo.windowTexture
        val window = gameWindowsManager.currentWindow as CreativeInventoryTabsWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - (windowTexture.regionHeight + selectedTabTexture.regionHeight) / 2

        val oneScrollAmount = GameWindowsConfigs.CreativeTabs.scrollIndicatorFullHeight / window.getMaxScroll()

        spriteBatch.draw(windowTexture, windowX, windowY)
        spriteBatch.draw(
            /* region = */ scrollIndicatorTexture,
            /* x = */ windowX + GameWindowsConfigs.CreativeTabs.scrollIndicatorMarginLeft,
            /* y = */
            windowY + GameWindowsConfigs.CreativeTabs.scrollIndicatorMarginTop +
                (gameWindowsManager.creativeScrollAmount * oneScrollAmount),
        )

        val allItems = itemsRepository.getAllItems().filter { window.selectedTab.isRelevantItem(it) }
        val startIndex = gameWindowsManager.creativeScrollAmount * GameWindowsConfigs.CreativeTabs.itemsInRow
        val endIndex = min(startIndex + GameWindowsConfigs.CreativeTabs.itemsOnPage, allItems.size)
        val items = sequence {
            for (i in startIndex..<endIndex) {
                yield(allItems.elementAt(i))
            }
        }

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.CreativeTabs.itemsGridMarginTopItemsTab,
            items = items.asIterable(),
            itemsInRow = GameWindowsConfigs.CreativeTabs.itemsInRow,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.CreativeTabs.hotbarOffsetFromBottom,
            items = mobController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.CreativeTabs.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.CreativeTabs.hotbarCells,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )
    }

    private fun drawInventoryTabContent(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
    ) {
        val windowTexture = currentTabInfo.windowTexture

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - (windowTexture.regionHeight + selectedTabTexture.regionHeight) / 2

        spriteBatch.draw(windowTexture, windowX, windowY)
        drawPlayerPortrait(spriteBatch, windowX, windowY)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.CreativeTabs.itemsGridMarginTop,
            items = mobController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.CreativeTabs.hotbarCells)
                .take(GameWindowsConfigs.CreativeTabs.itemsInCol * GameWindowsConfigs.CreativeTabs.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.CreativeTabs.itemsInRow,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.CreativeTabs.hotbarOffsetFromBottom,
            items = mobController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.CreativeTabs.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.CreativeTabs.hotbarCells,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.headX,
            gridY = windowY + GameWindowsConfigs.CreativeTabs.headY,
            items = listOf(mobController.player.wearingArmor.helmet),
            itemsInRow = 1,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.bodyX,
            gridY = windowY + GameWindowsConfigs.CreativeTabs.bodyY,
            items = listOf(mobController.player.wearingArmor.chestplate),
            itemsInRow = 1,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.legsX,
            gridY = windowY + GameWindowsConfigs.CreativeTabs.legsY,
            items = listOf(mobController.player.wearingArmor.leggings),
            itemsInRow = 1,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.CreativeTabs.feetX,
            gridY = windowY + GameWindowsConfigs.CreativeTabs.feetY,
            items = listOf(mobController.player.wearingArmor.boots),
            itemsInRow = 1,
            cellWidth = GameWindowsConfigs.CreativeTabs.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.CreativeTabs.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = currentTabInfo.windowTexture
        val window = gameWindowsManager.currentWindow as CreativeInventoryTabsWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - (windowTexture.regionHeight + selectedTabTexture.regionHeight) / 2

        CreativeInventoryTabsWindow.Tab.entries.forEachIndexed { index, tab ->
            val info = tabs[tab] ?: return@forEachIndexed

            spriteBatch.draw(unselectedTabTexture, windowX + 28f * index, windowY + windowTexture.regionHeight - 6f)
            spriteBatch.drawSprite(
                sprite = info.tabIconSprite,
                x = windowX + 28f * index + 6f,
                y = windowY + windowTexture.regionHeight,
                width = info.tabIconSprite.regionWidth.toFloat(),
                height = info.tabIconSprite.regionHeight.toFloat(),
            )
        }

        if (window.selectedTab.isInventory) {
            drawInventoryTabContent(spriteBatch, shapeRenderer, viewport)
        } else {
            drawItemTabContent(spriteBatch, shapeRenderer, viewport)
        }

        CreativeInventoryTabsWindow.Tab.entries.forEachIndexed { index, tab ->
            if (tab != window.selectedTab) {
                return@forEachIndexed
            }
            val info = tabs[tab] ?: return@forEachIndexed

            spriteBatch.draw(selectedTabTexture, windowX + 28f * index, windowY + windowTexture.regionHeight - 3f)
            spriteBatch.drawSprite(
                sprite = info.tabIconSprite,
                x = windowX + 28f * index + 6f,
                y = windowY + windowTexture.regionHeight + 2f,
                width = info.tabIconSprite.regionWidth.toFloat(),
                height = info.tabIconSprite.regionHeight.toFloat(),
            )
        }

        window.selectedItem?.let { selectedItem ->
            selectedItem.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                font = getFont(),
                x = Gdx.input.x * (viewport.width / Gdx.graphics.width) - 10f,
                y = Gdx.input.y * (viewport.height / Gdx.graphics.height) - 10f,
                getStringWidth = getStringWidth::invoke,
                getStringHeight = getStringHeight::invoke,
                width = 20f,
                height = 20f,
            )

            val itemName = selectedItem.item.params.name
            spriteBatch.drawString(
                font = getFont(),
                str = itemName,
                x = viewport.width / 2f - getStringWidth(itemName) / 2f,
                y = windowY + windowTexture.regionHeight + getStringHeight(itemName) + selectedTabTexture.regionHeight,
            )
        }
    }

    companion object {
        private const val CREATIVE_INVENTORY_TAB_KEY = "creative_inventory_tab"
        private const val CREATIVE_ITEMS_TAB_KEY = "creative_items_tab"
        private const val CREATIVE_SEARCH_TAB_KEY = "creative_search_tab"
        private const val CREATIVE_TAB_UNSELECTED_KEY = "creative_tab_unselected"
        private const val CREATIVE_TAB_SELECTED_KEY = "creative_tab_selected"
        private const val INVENTORY_CHAR_KEY = "inventory_char"
        private const val SCROLL_INDICATOR_KEY = "handle"
    }
}
