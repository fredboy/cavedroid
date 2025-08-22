package ru.fredboy.cavedroid.domain.save.model

import com.badlogic.gdx.files.FileHandle
import ru.fredboy.cavedroid.common.model.GameMode

data class GameSaveInfo(
    val version: Int,
    val name: String,
    val directory: String,
    val timeCreated: String,
    val gameMode: GameMode,
    val isSupported: Boolean,
    val screenshotHandle: FileHandle?,
)
