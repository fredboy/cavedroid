package ru.fredboy.cavedroid.common.model

enum class GameMode {
    SURVIVAL,
    CREATIVE,
    ;

    fun isSurvival() = this == SURVIVAL

    fun isCreative() = this == CREATIVE
}
