package ru.fredboy.cavedroid.game.window

import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.game.window.generated.TouchWindowGeometry
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid
import ru.fredboy.cavedroid.game.window.inventory.ChestInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import kotlin.math.min

/** Maps the baked PNG's geometry to the HUD; rendering and input use the same coordinates. */
class TouchWindowLayout(width: Float, height: Float, window: AbstractInventoryWindow) {
    enum class Kind { INVENTORY, ARMOR, CRAFT, RESULT, CHEST, INPUT, FUEL, RECIPE, CATALOG }

    data class Slot(val kind: Kind, val index: Int, val bounds: Rectangle)

    val background = when (window.type) {
        GameWindowType.CRAFTING_TABLE -> "crafting_table"
        GameWindowType.SURVIVAL_INVENTORY -> "survival"
        GameWindowType.CHEST -> if ((window as? ChestInventoryWindow)?.isLarge == true) "chest_large" else "chest"
        GameWindowType.FURNACE -> "furnace"
        GameWindowType.CREATIVE_INVENTORY -> "creative"
        GameWindowType.CREATIVE_INVENTORY_TABS -> if ((window as CreativeInventoryTabsWindow).selectedTab.isInventory) "creative_inventory" else "creative_items"
        GameWindowType.NONE, GameWindowType.SCENE2D -> error("No inventory touch window is open")
    } + if ((window as? AbstractInventoryWindowWithCraftGrid)?.recipeBookActive == true) "_recipes" else ""

    private val geometry = TouchWindowGeometry.layouts.getValue(background)
    val designHeight = geometry.height.toFloat()
    val bounds = fitBounds(width, height, designHeight)
    val scale = bounds.width / WIDTH
    val headerHeight = TouchWindowGeometry.HEADER_HEIGHT * scale
    val footerHeight = FOOTER_HEIGHT * scale

    private fun scaled(rect: Rectangle) = Rectangle(bounds.x + rect.x * scale, bounds.y + rect.y * scale, rect.width * scale, rect.height * scale)
    private fun control(key: String) = geometry.controls[key]?.let(::scaled) ?: Rectangle()

    val close = control("close")
    val drop = control("drop")
    val left = control("left")
    val right = control("right")
    val recipeToggle = control("recipeToggle")
    val previous = control("previous")
    val next = control("next")
    val recipePanel = control("recipePanel")
    val portrait = control("portrait")
    val arrow = control("arrow")
    val flame = control("flame")
    val scroll = control("scroll")
    val tabs = geometry.tabs.map(::scaled)
    val labels = geometry.labels.mapValues { (_, point) -> (bounds.x + point.first * scale) to (bounds.y + point.second * scale) }
    val slots = buildList {
        (geometry.inventory + geometry.grids).forEach { grid ->
            repeat(grid.rows) { row ->
                repeat(grid.columns) { column ->
                    add(
                        Slot(
                            Kind.valueOf(grid.kind),
                            grid.start + row * grid.stride + column,
                            scaled(Rectangle((grid.x + column * grid.width).toFloat(), (grid.y + row * grid.height).toFloat(), grid.width.toFloat(), grid.height.toFloat())),
                        ),
                    )
                }
            }
        }
    }

    val catalogColumns = geometry.grids.firstOrNull { it.kind == "CATALOG" }?.columns ?: 0
    val catalogRows = geometry.grids.firstOrNull { it.kind == "CATALOG" }?.rows ?: 0

    fun maxCatalogScroll(itemCount: Int): Int = if (catalogColumns > 0) {
        ((itemCount + catalogColumns - 1) / catalogColumns - catalogRows).coerceAtLeast(0)
    } else {
        0
    }

    fun slotAt(x: Float, y: Float): Slot? = slots.firstOrNull {
        x >= it.bounds.x && x < it.bounds.x + it.bounds.width && y >= it.bounds.y && y < it.bounds.y + it.bounds.height
    }

    companion object {
        const val WIDTH = TouchWindowGeometry.WIDTH
        const val HEIGHT = TouchWindowGeometry.HEIGHT
        const val DROP_WIDTH = 112f
        const val FOOTER_HEIGHT = TouchWindowGeometry.FOOTER_HEIGHT
        const val MIN_BUTTON_SIZE = TouchWindowGeometry.MIN_BUTTON_SIZE
        const val RECIPES_PER_PAGE = TouchWindowGeometry.RECIPES_PER_PAGE

        fun scaleFor(width: Float, height: Float) = min(width / WIDTH, height / HEIGHT)

        fun fitBounds(width: Float, height: Float, designHeight: Float = HEIGHT): Rectangle {
            val scale = min(width / WIDTH, height / designHeight)
            val fitWidth = WIDTH * scale
            val fitHeight = designHeight * scale
            return Rectangle((width - fitWidth) / 2, (height - fitHeight) / 2, fitWidth, fitHeight)
        }
    }
}
