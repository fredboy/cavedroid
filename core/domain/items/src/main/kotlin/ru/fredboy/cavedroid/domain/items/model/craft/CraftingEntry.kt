package ru.fredboy.cavedroid.domain.items.model.craft

import ru.fredboy.cavedroid.domain.items.model.item.Item

data class CraftingEntry(
    val recipes: List<CraftingRecipe>,
    val result: Item,
)
