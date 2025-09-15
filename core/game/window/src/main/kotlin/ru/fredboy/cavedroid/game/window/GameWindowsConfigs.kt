package ru.fredboy.cavedroid.game.window

object GameWindowsConfigs {
    data object Creative {
        const val scrollIndicatorMarginLeft = 156f
        const val scrollIndicatorMarginTop = 18f
        const val scrollIndicatorFullHeight = 72f

        const val itemsGridMarginLeft = 8f
        const val itemsGridMarginTop = 18f

        const val itemsGridRowHeight = 18f
        const val itemsGridColWidth = 18f

        const val itemsInRow = 8
        const val itemsInCol = 5

        const val invItems = 9

        const val playerInventoryOffsetFromBottom = 24f

        val itemsOnPage get() = itemsInCol * itemsInRow
    }

    data object Survival {
        const val itemsGridMarginLeft = 8f
        const val itemsGridMarginTop = 84f

        const val itemsGridRowHeight = 18f
        const val itemsGridColWidth = 18f

        const val itemsInRow = 9
        const val itemsInCol = 5

        const val hotbarOffsetFromBottom = 24f
        const val hotbarCells = 9

        const val portraitMarginLeft = 24f
        const val portraitMarginTop = 8f
        const val portraitWidth = 48f
        const val portraitHeight = 68f

        const val helmX = 8f
        const val helmY = 0f

        const val chestX = 0f
        const val chestY = 16f

        const val pantX = 8f
        const val pantY = 36f

        const val bootX = 8f
        const val bootY = 50f

        const val craftGridSize = 2

        const val recipeButtonX = 76f
        const val recipeButtonY = 27f
        const val recipeButtonWidth = 22f
        const val recipeButtonHeight = 19f

        const val craftOffsetX = 98f
        const val craftOffsetY = 18f

        const val craftResultOffsetX = 154f
        const val craftResultOffsetY = 28f

        const val armorGridOffsetX = 8f
        const val armorGridOffsetY = 8f
    }

    data object Crafting {
        const val itemsGridMarginLeft = 8f
        const val itemsGridMarginTop = 84f

        const val itemsGridRowHeight = 18f
        const val itemsGridColWidth = 18f

        const val itemsInRow = 9
        const val itemsInCol = 5

        const val hotbarOffsetFromBottom = 24f
        const val hotbarCells = 9

        const val craftGridSize = 3

        const val craftOffsetX = 30f
        const val craftOffsetY = 18f

        const val craftResultOffsetX = 124f
        const val craftResultOffsetY = 36f

        const val recipeButtonX = 6f
        const val recipeButtonY = 34f
        const val recipeButtonWidth = 22f
        const val recipeButtonHeight = 19f
    }

    data object Furnace {
        const val itemsGridMarginLeft = 8f
        const val itemsGridMarginTop = 84f

        const val itemsGridRowHeight = 18f
        const val itemsGridColWidth = 18f

        const val itemsInRow = 9
        const val itemsInCol = 5

        const val hotbarOffsetFromBottom = 24f
        const val hotbarCells = 9

        const val smeltInputMarginLeft = 56f
        const val smeltInputMarginTop = 18f

        const val smeltFuelMarginLeft = 56f
        const val smeltFuelMarginTop = 54f

        const val smeltResultOffsetX = 116f
        const val smeltResultOffsetY = 36f

        const val fuelBurnMarginLeft = 56f
        const val fuelBurnMarginTop = 36f
        const val fuelBurnHeight = 14f

        const val progressMarginLeft = 79f
        const val progressMarginTop = 34f
        const val progressWidth = 24f
    }

    data object Chest {
        const val itemsGridMarginLeft = 8f
        const val itemsGridMarginTop = 86f

        const val itemsGridRowHeight = 18f
        const val itemsGridColWidth = 18f

        const val hotbarCells = 9
        const val hotbarOffsetFromBottom = 24f

        const val itemsInRow = 9
        const val itemsInCol = 5

        const val contentsMarginLeft = 8f
        const val contentsMarginTop = 18f

        const val contentsInRow = 9
        const val contentsInCol = 3
    }

    data object RecipeBook {
        const val gridWidth = 5
        const val gridHeight = 5
        const val pageSize = gridWidth * gridHeight

        const val gridX = 12f
        const val gridY = 12f

        const val cellSize = 25f

        const val prevX = 28f
        const val prevY = 143f

        const val nextX = 98f
        const val nextY = 143f
    }
}
