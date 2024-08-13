package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.Serializable

@Serializable
data class CraftingDto(
    val input: List<String>,
    val count: Int = 1,
)
