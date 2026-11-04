package ru.fredboy.cavedroid.common.api

import com.badlogic.gdx.files.FileHandle

/** Native archive access. Picked files are temporary copies owned by the caller. */
interface AssetPackPlatform {
    val isSupported: Boolean
    fun pickArchive(onResult: (Result<FileHandle?>) -> Unit)
    fun extractArchive(archive: FileHandle, destination: FileHandle): String
}

object NoOpAssetPackPlatform : AssetPackPlatform {
    override val isSupported = false
    override fun pickArchive(onResult: (Result<FileHandle?>) -> Unit) = onResult(Result.success(null))
    override fun extractArchive(archive: FileHandle, destination: FileHandle): String = error("Asset packs are unavailable")
}
