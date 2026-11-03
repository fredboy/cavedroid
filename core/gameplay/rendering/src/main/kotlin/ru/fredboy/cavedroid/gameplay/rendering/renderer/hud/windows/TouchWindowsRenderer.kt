package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.WearableTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.TouchWindowLayout
import ru.fredboy.cavedroid.game.window.TouchWindowLayout.Kind
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithScroll
import ru.fredboy.cavedroid.game.window.inventory.ChestInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.game.window.inventory.FurnaceInventoryWindow
import javax.inject.Inject
import kotlin.math.min

/** Draws the baked touch-window PNG, then dynamic items, localized text and state overlays. */
@GameScope
class TouchWindowsRenderer @Inject constructor(
    private val windows: GameWindowsManager,
    private val mobs: MobController,
    private val textures: GetTextureRegionByNameUseCase,
    private val fonts: FontTextureAssetsRepository,
    private val items: ItemsRepository,
    private val wearables: WearableTextureAssetsRepository,
) {
    private fun texture(key: String) = requireNotNull(textures[key])
    private var scaleX = 1f
    private var scaleY = 1f

    fun draw(batch: SpriteBatch, shapes: ShapeRenderer, viewport: Rectangle) {
        val window = windows.currentWindow ?: return
        val width = TouchWindowLayout.WIDTH
        val height = TouchWindowLayout(width, TouchWindowLayout.HEIGHT, window).designHeight
        val bounds = TouchWindowLayout.fitBounds(viewport.width, viewport.height, height)
        scaleX = bounds.width / width
        scaleY = scaleX
        val layout = TouchWindowLayout(width, height, window)
        val oldBatch = Matrix4(batch.transformMatrix)
        val oldShapes = Matrix4(shapes.transformMatrix)
        batch.transformMatrix = Matrix4(oldBatch).translate(bounds.x, bounds.y, 0f).scale(scaleX, scaleY, 1f)
        shapes.transformMatrix = Matrix4(oldShapes).translate(bounds.x, bounds.y, 0f).scale(scaleX, scaleY, 1f)
        try {
            batch.draw(texture("touch_${layout.background}"), 0f, 0f, width, height)
            val title = when (window.type) {
                GameWindowType.CRAFTING_TABLE -> items.getItemByKey("crafting_table").params.name
                GameWindowType.FURNACE -> items.getItemByKey("furnace").params.name
                GameWindowType.CHEST -> if ((window as ChestInventoryWindow).isLarge) label("large_chest") else items.getItemByKey("chest").params.name
                GameWindowType.CREATIVE_INVENTORY_TABS -> fonts.getMenuLocalizationBundle()[(window as CreativeInventoryTabsWindow).selectedTab.localizationKey]
                GameWindowType.CREATIVE_INVENTORY -> fonts.getMenuLocalizationBundle()["creative"]
                else -> label("inventory")
            }
            withUiFont { text(batch, title, 302f, (layout.headerHeight - fonts.getStringHeight(title)) / 2) }
            val trash = window is CreativeInventoryTabsWindow && window.selectedTab.isInventory
            centeredText(batch, layout.drop, if (trash) fonts.getMenuLocalizationBundle()["delete"] else label("drop"))
            withUiFont {
                layout.labels.forEach { (key, point) -> text(batch, label(key), point.first, point.second) }
            }
            if (window is AbstractInventoryWindowWithCraftGrid) {
                val toggle = layout.recipeToggle
                centeredText(batch, Rectangle(toggle.x + 30, toggle.y, toggle.width - 32, toggle.height), label("recipes"))
                if (window.recipeBookActive) {
                    batch.draw(texture("touch_recipe_toggle_active"), toggle.x, toggle.y, toggle.width, toggle.height)
                    val pages = ((window.getAvailableCraftingRecipes(items).count() + TouchWindowLayout.RECIPES_PER_PAGE - 1) / TouchWindowLayout.RECIPES_PER_PAGE).coerceAtLeast(1)
                    val page = "${window.recipeBookPage + 1} / $pages"
                    text(batch, page, layout.recipePanel.x + layout.recipePanel.width / 2 - fonts.getStringWidth(page) / 2, layout.previous.y + (layout.previous.height - fonts.getStringHeight(page)) / 2, Color.BLACK)
                }
            }
            val creative = window as? CreativeInventoryTabsWindow
            val catalog = if (window is AbstractInventoryWindowWithScroll) {
                creative?.getCatalogItems() ?: items.getAllItems().toList()
            } else {
                emptyList()
            }
            layout.tabs.forEachIndexed { index, rect ->
                val tab = CreativeInventoryTabsWindow.Tab.entries[index]
                val selected = tab == creative?.selectedTab
                batch.draw(texture(if (selected) "touch_selected_tab" else "touch_creative_tab"), rect.x, rect.y, rect.width, rect.height)
                val item = items.getItemByKey(tab.iconKey)
                val iconSize = min(rect.width, rect.height) - 8f
                batch.drawSprite(item.sprite, rect.x + (rect.width - iconSize) / 2, rect.y + (rect.height - iconSize) / 2, width = iconSize, height = iconSize)
            }
            val phantom = (window as? AbstractInventoryWindowWithCraftGrid)?.getPhantomRecipe(items, TouchWindowLayout.RECIPES_PER_PAGE).orEmpty()
            val recipes = (window as? AbstractInventoryWindowWithCraftGrid)?.getVisibleCraftingRecipes(items, TouchWindowLayout.RECIPES_PER_PAGE).orEmpty()
            layout.slots.forEach { slot ->
                val rect = slot.bounds
                if (slot.kind == Kind.RECIPE && slot.index == (window as AbstractInventoryWindowWithCraftGrid).selectedRecipe) {
                    batch.draw(texture("touch_selected_slot"), rect.x, rect.y, rect.width, rect.height)
                }
                val item = when (slot.kind) {
                    Kind.INVENTORY -> mobs.player.inventory.items[slot.index]
                    Kind.ARMOR -> mobs.player.wearingArmor.items[slot.index]
                    Kind.CHEST -> (window as ChestInventoryWindow).items[slot.index]
                    Kind.CRAFT -> (window as AbstractInventoryWindowWithCraftGrid).craftingItems[slot.index].takeUnless { it.isNoneOrNull() } ?: phantom.getOrNull(slot.index)
                    Kind.RESULT -> if (window is FurnaceInventoryWindow) window.furnace.result else (window as AbstractInventoryWindowWithCraftGrid).craftResult
                    Kind.INPUT -> (window as FurnaceInventoryWindow).furnace.input
                    Kind.FUEL -> (window as FurnaceInventoryWindow).furnace.fuel
                    Kind.RECIPE -> recipes.getOrNull(slot.index)?.second?.toInventoryItem()
                    Kind.CATALOG -> catalog.getOrNull(slot.index + windows.creativeScrollAmount * layout.catalogColumns)?.let { it.toInventoryItem(durability = (it as? Item.Durable)?.durability ?: 1) }
                }
                item?.let { drawItem(batch, shapes, it, rect) }
            }
            if (layout.portrait.width > 0) drawPortrait(batch, layout.portrait)
            if (window is FurnaceInventoryWindow && window.furnace.isActive) {
                val furnace = window.furnace
                val flame = layout.flame
                val remaining = 1f - furnace.burnProgress.coerceIn(0f, 1f)
                val burn = TextureRegion(texture("touch_furnace_burn")).apply { v2 = v + (v2 - v) * remaining }
                batch.draw(burn, flame.x, flame.y + flame.height * (1f - remaining), flame.width, flame.height * remaining)
                if (furnace.canSmelt()) {
                    val arrow = layout.arrow
                    val progress = furnace.smeltProgress.coerceIn(0f, 1f)
                    val region = texture("smelting_progress")
                    val cropped = TextureRegion(region).apply { u2 = u + (u2 - u) * progress }
                    batch.draw(cropped, arrow.x, arrow.y, arrow.width * progress, arrow.height)
                }
            }
            if (layout.scroll.width > 0) {
                val max = layout.maxCatalogScroll(catalog.size).coerceAtLeast(1)
                val rect = layout.scroll
                val thumbHeight = min(rect.height, TouchWindowLayout.MIN_BUTTON_SIZE)
                batch.draw(texture("touch_handle"), rect.x, rect.y + (rect.height - thumbHeight) * windows.creativeScrollAmount / max, rect.width, thumbHeight)
            }
            window.selectedItem?.takeUnless { it.isNoneOrNull() }?.let { held ->
                val pointer = window.selectItemPointer.coerceAtLeast(0)
                val x = (Gdx.input.getX(pointer).toFloat() / Gdx.graphics.width * viewport.width - bounds.x) / scaleX
                val y = (Gdx.input.getY(pointer).toFloat() / Gdx.graphics.height * viewport.height - bounds.y) / scaleY
                layout.slotAt(x, y)?.takeIf { it.kind != Kind.RECIPE && it.kind != Kind.CATALOG }?.bounds?.let { target ->
                    batch.draw(texture("touch_selected_slot"), target.x, target.y, target.width, target.height)
                }
                drawItem(batch, shapes, held, Rectangle(x - 10f, (y - 30f).coerceAtLeast(0f), 22f, 22f))
                val name = held.item.params.name
                withUiFont {
                    if (fonts.getStringWidth(name) < layout.drop.x - 20f) {
                        text(batch, name, 12f, height - layout.footerHeight + (layout.footerHeight - fonts.getStringHeight(name)) / 2)
                    }
                }
            }
        } finally {
            batch.transformMatrix = oldBatch
            shapes.transformMatrix = oldShapes
        }
    }

    private fun label(key: String) = fonts.getMenuLocalizationBundle()["window_$key"]

    private fun text(batch: SpriteBatch, value: String, x: Float, y: Float, color: Color = Color.WHITE) {
        batch.drawString(fonts.getFont(), value, x, y, color)
    }

    private fun centeredText(batch: SpriteBatch, rect: Rectangle, caption: String) = withUiFont {
        text(batch, caption, rect.x + (rect.width - fonts.getStringWidth(caption)) / 2, rect.y + (rect.height - fonts.getStringHeight(caption)) / 2)
    }

    private inline fun withUiFont(draw: () -> Unit) {
        val font = fonts.getFont()
        val oldX = font.data.scaleX
        val oldY = font.data.scaleY
        font.data.setScale(oldX * 1.25f, oldY * 1.25f)
        try {
            draw()
        } finally {
            font.data.setScale(oldX, oldY)
        }
    }

    private fun drawItem(batch: SpriteBatch, shapes: ShapeRenderer, item: InventoryItem, rect: Rectangle) {
        val oldBatch = Matrix4(batch.transformMatrix)
        val oldShapes = Matrix4(shapes.transformMatrix)
        val itemScale = min(rect.width * scaleX, rect.height * scaleY) / 18f
        val localX = itemScale / scaleX
        val localY = itemScale / scaleY
        val x = rect.x + (rect.width - 18 * localX) / 2
        val y = rect.y + (rect.height - 18 * localY) / 2
        batch.transformMatrix = Matrix4(oldBatch).translate(x, y, 0f).scale(localX, localY, 1f)
        shapes.transformMatrix = Matrix4(oldShapes).translate(x, y, 0f).scale(localX, localY, 1f)
        val font = fonts.getFont()
        val oldFontX = font.data.scaleX
        val oldFontY = font.data.scaleY
        font.data.setScale(oldFontX * .7f, oldFontY * .7f)
        try {
            item.draw(batch, shapes, font, 1f, 1f, fonts::getStringWidth, fonts::getStringHeight, 16f, 16f)
        } finally {
            font.data.setScale(oldFontX, oldFontY)
            batch.transformMatrix = oldBatch
            shapes.transformMatrix = oldShapes
        }
    }

    private fun drawPortrait(batch: SpriteBatch, rect: Rectangle) {
        val portrait = texture("inventory_char")
        val scale = min(rect.width * scaleX / portrait.regionWidth, rect.height * scaleY / portrait.regionHeight)
        val x = rect.x + (rect.width - portrait.regionWidth * scale / scaleX) / 2
        val y = rect.y + (rect.height - portrait.regionHeight * scale / scaleY) / 2
        batch.draw(portrait, x, y, portrait.regionWidth * scale / scaleX, portrait.regionHeight * scale / scaleY)
        mobs.player.wearingArmor.items.forEachIndexed { index, inventoryItem ->
            val armor = inventoryItem.item as? Item.Armor ?: return@forEachIndexed
            val offset = when (armor) {
                is Item.Helmet -> 8f to 0f
                is Item.Chestplate -> 0f to 16f
                is Item.Leggings -> 8f to 36f
                is Item.Boots -> 8f to 50f
            }
            wearables.getFrontSprite(armor.material, index)?.let { sprite ->
                batch.drawSprite(sprite, x + offset.first * scale / scaleX, y + offset.second * scale / scaleY, width = sprite.regionWidth * scale / scaleX, height = sprite.regionHeight * scale / scaleY, tint = armor.params.tint)
            }
        }
    }
}
