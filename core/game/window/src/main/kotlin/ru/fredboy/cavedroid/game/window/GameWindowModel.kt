package ru.fredboy.cavedroid.game.window

/** Session-owned window state. Actors and platform input belong to the view layer. */
interface GameWindowModel {
    val key: String

    fun onClose() = Unit
}
