package ru.fredboy.cavedroid.gdx.menu.v2.view.worldconfig

import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.mvvm.NavKey

data class WorldConfigMenuNavKey(
    val worldName: String,
    val gameMode: GameMode,
    val seed: Long,
) : NavKey
