package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

import com.badlogic.gdx.graphics.Texture
import ru.fredboy.cavedroid.common.model.GameMode

class SaveInfoVo(
    val version: Int,
    val name: String,
    val directory: String,
    val timeCreated: String,
    val gameMode: GameMode,
    val screenshot: Texture?,
)
