package ru.fredboy.cavedroid.domain.items.model.craft

import kotlin.collections.chunked
import kotlin.sequences.chunked
import kotlin.sequences.map
import kotlin.sequences.maxOf

data class CraftingRecipe(
    val input: List<Regex?>,
    val isShapeless: Boolean,
    val amount: Int,
) {

    fun getHeight(): Int {
        return input.chunked(3)
            .dropWhile { it.all { item -> item == null } }
            .dropLastWhile { it.all { item -> item == null } }
            .size
    }

    fun getWidth(): Int {
        return input.asSequence()
            .chunked(3)
            .map { row ->
                row.dropWhile { it == null }
                    .dropLastWhile { it == null }
            }
            .maxOf { row -> row.size }
    }
}
