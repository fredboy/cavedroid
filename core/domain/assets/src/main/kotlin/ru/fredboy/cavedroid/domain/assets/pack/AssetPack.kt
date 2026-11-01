package ru.fredboy.cavedroid.domain.assets.pack

import com.badlogic.gdx.files.FileHandle

/** Version of the asset-pack layout and data contract, independent of game releases. */
object AssetPackFormat {
    const val VERSION = 1
}

data class PackMetadata(
    val formatVersion: Int,
    val name: String,
    val version: String,
    val author: String,
    val license: String,
    val icon: String,
    val textureScales: Map<String, Int> = emptyMap(),
)

data class InstalledAssetPack(val id: String, val metadata: PackMetadata, val directory: FileHandle)

interface AssetPackRepository {
    val isSupported: Boolean
    val runningIds: List<String>
    val pendingIds: List<String>
    val startupError: String?
    fun installed(): List<InstalledAssetPack>
    fun importArchive(archive: FileHandle): InstalledAssetPack
    fun saveSelection(ids: List<String>)
    fun remove(id: String)

    /** Delete retired pack files only after no active asset generation uses them. */
    fun cleanupRemoved()
    fun disableAfterFailure(message: String)
}
