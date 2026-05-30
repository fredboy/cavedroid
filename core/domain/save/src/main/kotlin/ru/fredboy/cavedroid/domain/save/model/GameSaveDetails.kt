package ru.fredboy.cavedroid.domain.save.model

import com.badlogic.gdx.files.FileHandle
import ru.fredboy.cavedroid.common.model.GameMode

data class GameSaveDetails(
    val name: String,
    val directory: String,
    val gameMode: GameMode,
    val widthBlocks: Int,
    val heightBlocks: Int,
    val sizeBytes: Long,
    val version: Int,
    val isSupported: Boolean,
    val seed: Long?,
    val lastModifiedString: String,
    val createdString: String?,
    val screenshotHandle: FileHandle?,
)
