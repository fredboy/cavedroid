package ru.deadsoftware.cavedroid.game.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class CraftingDto(
    val input: List<String>,
    val count: Int = 1,
)
