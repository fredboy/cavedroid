package ru.fredboy.cavedroid.domain.save.model

data class GrowBlockEntry(
    val x: Int,
    val y: Int,
    val key: String,
    val remainingTicks: Long,
)
