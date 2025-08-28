package ru.fredboy.cavedroid.domain.items.model.drop

sealed interface DropAmount {

    data class ExactAmount(
        val amount: Int,
    ) : DropAmount

    data class RandomRange(
        val range: IntRange,
        val chance: Float,
    ) : DropAmount

    data class RandomChance(
        val chance: Float,
        val amount: Int,
    ) : DropAmount
}
