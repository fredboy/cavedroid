package ru.fredboy.cavedroid.domain.items.model.craft

data class CraftingRecipe(
    val input: List<Regex?>,
    val isShapeless: Boolean,
    val amount: Int,
)
