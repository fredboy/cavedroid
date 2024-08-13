package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameItemsDto(
    @SerialName("blocks") val blocks: Map<String, BlockDto>,
    @SerialName("items") val items: Map<String, ItemDto>,
)