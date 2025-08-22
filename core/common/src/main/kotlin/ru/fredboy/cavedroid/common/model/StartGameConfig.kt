package ru.fredboy.cavedroid.common.model

sealed interface StartGameConfig {

    val worldName: String

    val saveDirectory: String

    data class New(
        override val worldName: String,
        override val saveDirectory: String,
        val gameMode: GameMode,
    ) : StartGameConfig

    data class Load(
        override val worldName: String,
        override val saveDirectory: String,
    ) : StartGameConfig
}
