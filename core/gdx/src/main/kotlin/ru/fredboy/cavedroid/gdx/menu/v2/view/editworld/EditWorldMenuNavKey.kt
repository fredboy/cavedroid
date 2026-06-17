package ru.fredboy.cavedroid.gdx.menu.v2.view.editworld

import ru.fredboy.cavedroid.common.mvvm.NavKey

data class EditWorldMenuNavKey(
    val worldName: String,
    val saveDirectory: String,
) : NavKey
