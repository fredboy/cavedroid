package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface DropAmountDto {

    @Serializable
    @SerialName("exact_amount")
    data class ExactAmount(
        @SerialName("amount") val amount: Int,
    ) : DropAmountDto

    @Serializable
    @SerialName("random_range")
    data class RandomRange(
        @SerialName("min") val min: Int,
        @SerialName("max") val max: Int,
        @SerialName("chance") val chance: Float,
    ) : DropAmountDto

    @Serializable
    @SerialName("random_chance")
    data class RandomChance(
        @SerialName("chance") val chance: Float,
        @SerialName("amount") val amount: Int,
    ) : DropAmountDto
}
