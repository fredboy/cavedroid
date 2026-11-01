// Coordinates of the baked touch-window PNG assets.
package ru.fredboy.cavedroid.game.window.generated

import com.badlogic.gdx.math.Rectangle

internal object TouchWindowGeometry {
    const val WIDTH = 640f
    const val HEIGHT = 320f
    const val HEADER_HEIGHT = 48f
    const val FOOTER_HEIGHT = 48f
    const val MIN_BUTTON_SIZE = 44f
    const val RECIPES_PER_PAGE = 24
    data class Grid(val kind: String, val x: Int, val y: Int, val columns: Int, val rows: Int, val width: Int, val height: Int, val start: Int, val stride: Int)
    data class Definition(val grids: List<Grid>, val controls: Map<String, Rectangle>, val labels: Map<String, Pair<Int, Int>>, val tabs: List<Rectangle>)
    val inventory = listOf(
        Grid("INVENTORY", 302, 76, 9, 3, 36, 36, 9, 9),
        Grid("INVENTORY", 302, 224, 9, 1, 36, 36, 0, 9),
    )
    val layouts = mapOf(
        "crafting_table" to Definition(
            grids = listOf(
                Grid("CRAFT", 12, 20, 3, 3, 52, 52, 0, 3),
                Grid("RESULT", 210, 94, 1, 1, 56, 56, 0, 1),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "recipeToggle" to Rectangle(174f, 8f, 104f, 44f),
                "arrow" to Rectangle(174f, 110f, 30f, 22f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = emptyList(),
        ),
        "crafting_table_recipes" to Definition(
            grids = listOf(
                Grid("CRAFT", 12, 10, 3, 3, 36, 36, 0, 3),
                Grid("RESULT", 216, 76, 1, 1, 44, 44, 0, 1),
                Grid("RECIPE", 24, 138, 8, 3, 30, 28, 0, 8),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "recipeToggle" to Rectangle(174f, 8f, 104f, 44f),
                "arrow" to Rectangle(160f, 88f, 40f, 20f),
                "recipePanel" to Rectangle(12f, 134f, 264f, 138f),
                "previous" to Rectangle(36f, 226f, 88f, 44f),
                "next" to Rectangle(164f, 226f, 88f, 44f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = emptyList(),
        ),
        "survival" to Definition(
            grids = listOf(
                Grid("ARMOR", 12, 18, 1, 4, 36, 36, 0, 1),
                Grid("CRAFT", 144, 72, 2, 2, 36, 36, 0, 3),
                Grid("RESULT", 236, 88, 1, 1, 36, 36, 0, 1),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "portrait" to Rectangle(66f, 20f, 64f, 128f),
                "recipeToggle" to Rectangle(174f, 8f, 104f, 44f),
                "arrow" to Rectangle(210f, 96f, 20f, 20f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = emptyList(),
        ),
        "survival_recipes" to Definition(
            grids = listOf(
                Grid("ARMOR", 12, 10, 1, 4, 28, 28, 0, 1),
                Grid("CRAFT", 120, 66, 2, 2, 28, 28, 0, 3),
                Grid("RESULT", 236, 82, 1, 1, 36, 36, 0, 1),
                Grid("RECIPE", 24, 138, 8, 3, 30, 28, 0, 8),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "portrait" to Rectangle(56f, 10f, 56f, 112f),
                "recipeToggle" to Rectangle(174f, 8f, 104f, 44f),
                "arrow" to Rectangle(188f, 88f, 32f, 24f),
                "recipePanel" to Rectangle(12f, 134f, 264f, 138f),
                "previous" to Rectangle(36f, 226f, 88f, 44f),
                "next" to Rectangle(164f, 226f, 88f, 44f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = emptyList(),
        ),
        "chest" to Definition(
            grids = listOf(
                Grid("CHEST", 10, 12, 9, 3, 30, 30, 0, 9),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = emptyList(),
        ),
        "furnace" to Definition(
            grids = listOf(
                Grid("INPUT", 56, 40, 1, 1, 44, 44, 0, 1),
                Grid("FUEL", 56, 156, 1, 1, 44, 44, 0, 1),
                Grid("RESULT", 202, 92, 1, 1, 48, 48, 0, 1),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "flame" to Rectangle(64f, 98f, 28f, 40f),
                "arrow" to Rectangle(124f, 106f, 56f, 24f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = emptyList(),
        ),
        "creative" to Definition(
            grids = listOf(
                Grid("CATALOG", 12, 12, 6, 7, 36, 36, 0, 6),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "scroll" to Rectangle(232f, 12f, 44f, 252f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = emptyList(),
        ),
        "creative_items" to Definition(
            grids = listOf(
                Grid("CATALOG", 12, 106, 6, 4, 36, 36, 0, 6),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "scroll" to Rectangle(232f, 106f, 44f, 144f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = listOf(
                Rectangle(12f, 8f, 64f, 44f),
                Rectangle(76f, 8f, 64f, 44f),
                Rectangle(140f, 8f, 64f, 44f),
                Rectangle(204f, 8f, 64f, 44f),
                Rectangle(12f, 54f, 64f, 44f),
                Rectangle(76f, 54f, 64f, 44f),
                Rectangle(140f, 54f, 64f, 44f),
                Rectangle(204f, 54f, 64f, 44f),
            ),
        ),
        "creative_inventory" to Definition(
            grids = listOf(
                Grid("ARMOR", 24, 112, 1, 4, 36, 36, 0, 1),
            ),
            controls = mapOf(
                "close" to Rectangle(592f, 2f, 44f, 44f),
                "drop" to Rectangle(528f, 274f, 108f, 44f),
                "left" to Rectangle(6f, 2f, 276f, 268f),
                "right" to Rectangle(292f, 50f, 342f, 220f),
                "portrait" to Rectangle(88f, 112f, 72f, 144f),
            ),
            labels = mapOf(
                "inventory" to (302 to 58),
                "hotbar" to (302 to 198),
            ),
            tabs = listOf(
                Rectangle(12f, 8f, 64f, 44f),
                Rectangle(76f, 8f, 64f, 44f),
                Rectangle(140f, 8f, 64f, 44f),
                Rectangle(204f, 8f, 64f, 44f),
                Rectangle(12f, 54f, 64f, 44f),
                Rectangle(76f, 54f, 64f, 44f),
                Rectangle(140f, 54f, 64f, 44f),
                Rectangle(204f, 54f, 64f, 44f),
            ),
        ),
    )
}
