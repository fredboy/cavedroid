package ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld

import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavKey

data class DeleteWorldMenuNavKey(
    val worldName: String,
    val saveDirectory: String,
) : NavKey
