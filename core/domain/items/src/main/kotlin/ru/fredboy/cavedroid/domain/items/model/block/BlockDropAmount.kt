package ru.fredboy.cavedroid.domain.items.model.block

sealed interface BlockDropAmount {

    data class ExactAmount(
        val amount: Int,
    ) : BlockDropAmount

    data class RandomRange(
        val range: IntRange,
        val chance: Float,
    ) : BlockDropAmount

    data class RandomChance(
        val chance: Float,
        val amount: Int,
    ) : BlockDropAmount
}
