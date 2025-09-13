package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.Serializable

@Serializable
data class CraftingRecipeDto(
    val input: List<String?>,
    val shapeless: Boolean,
    val count: Int,
)
