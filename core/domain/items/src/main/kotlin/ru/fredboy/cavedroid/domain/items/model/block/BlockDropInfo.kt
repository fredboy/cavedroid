package ru.fredboy.cavedroid.domain.items.model.block

data class BlockDropInfo(
    val itemKey: String,
    val requiresTool: Boolean,
    val amount: BlockDropAmount,
)
