package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockDropInfoDto(
    @SerialName("key") val key: String,
    @SerialName("amount") val amount: DropAmountDto = DropAmountDto.ExactAmount(1),
    @SerialName("requires_tool") val requiresTool: Boolean = true,
)
