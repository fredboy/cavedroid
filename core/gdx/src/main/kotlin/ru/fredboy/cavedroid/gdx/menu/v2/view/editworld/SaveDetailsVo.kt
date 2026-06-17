package ru.fredboy.cavedroid.gdx.menu.v2.view.editworld

import com.badlogic.gdx.graphics.Texture
import ru.fredboy.cavedroid.common.model.GameMode

class SaveDetailsVo(
    val name: String,
    val gameMode: GameMode,
    val mapSize: String,
    val diskSize: String,
    val version: Int,
    val isSupported: Boolean,
    val seed: String?,
    val created: String?,
    val lastModified: String,
    val screenshot: Texture?,
)
