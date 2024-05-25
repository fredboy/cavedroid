package ru.deadsoftware.cavedroid.game.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class GameItemsDto(
    @SerialName("blocks") val blocks: Map<String, BlockDto>,
    @SerialName("items") val items: Map<String, ItemDto>,
)