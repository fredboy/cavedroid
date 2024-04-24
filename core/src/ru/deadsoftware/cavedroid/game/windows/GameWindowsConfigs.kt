package ru.deadsoftware.cavedroid.game.windows

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

        const val craftGridSize = 2

        const val craftOffsetX = 98f
        const val craftOffsetY = 18f

        const val craftResultOffsetX = 154f
        const val craftResultOffsetY = 28f
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

        const val craftResultOffsetX = 128f
        const val craftResultOffsetY = 36f
    }
}