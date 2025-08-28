package ru.fredboy.cavedroid.domain.items.model.drop

data class DropInfo(
    val itemKey: String,
    val requiresTool: Boolean,
    val amount: DropAmount,
)
