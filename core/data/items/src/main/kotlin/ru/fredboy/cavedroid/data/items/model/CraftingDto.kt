package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.Serializable

@Serializable
data class CraftingDto(
    val recipes: List<CraftingRecipeDto>,
)
