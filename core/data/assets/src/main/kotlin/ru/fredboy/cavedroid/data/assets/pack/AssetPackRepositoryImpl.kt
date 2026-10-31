package ru.fredboy.cavedroid.data.assets.pack

import co.touchlab.kermit.Logger
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import kotlinx.serialization.json.Json
import ru.fredboy.cavedroid.common.api.AssetPackPlatform
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackRepository
import ru.fredboy.cavedroid.domain.assets.pack.InstalledAssetPack
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetPackRepositoryImpl @Inject constructor(
    private val platform: AssetPackPlatform,
    private val preferences: PreferencesStore,
    context: ApplicationContextRepository,
) : AssetPackRepository {
    override val isSupported get() = platform.isSupported
    private val root by lazy { Gdx.files.getFileHandle(context.getGameDirectory(), context.getGameDirectoryFileType()).child("asset-packs") }
    override val runningIds = if (isSupported) readSelection() else emptyList()
    override val pendingIds get() = if (isSupported) readSelection() else emptyList()
    override val startupError get() = preferences.getPreference(ERROR_KEY)

    init {
        if (isSupported) {
            root.mkdirs()
            root.list().filter { it.name().startsWith(".import-") || it.child(".removed").exists() }
                .forEach { it.deleteDirectory() }
        }
    }

    private fun readSelection(): List<String> = preferences.getPreference(SELECTION_KEY).orEmpty()
        .split(',').filter { it.matches(Regex("[0-9a-f]{64}")) }.distinct()

    override fun installed(): List<InstalledAssetPack> = if (!isSupported) {
        emptyList()
    } else {
        root.list()
            .filter { it.isDirectory && it.name().matches(Regex("[0-9a-f]{64}")) && !it.child(".removed").exists() }
            .mapNotNull { directory ->
                runCatching { InstalledAssetPack(directory.name(), PackFormat.metadata(directory.child("pack.json").readString("UTF-8")), directory) }
                    .onFailure { logger.w(it) { "Cannot read pack ${directory.name()}" } }.getOrNull()
            }.sortedWith(compareBy({ it.metadata.name }, { it.metadata.version }, { it.id }))
    }

    override fun importArchive(archive: FileHandle): InstalledAssetPack {
        check(isSupported)
        val temporary = root.child(".import-${System.nanoTime()}")
        try {
            temporary.mkdirs()
            val id = platform.extractArchive(archive, temporary)
            require(!temporary.child(".removed").exists()) { "Reserved pack entry: .removed" }
            val metadataFile = temporary.child("pack.json")
            require(metadataFile.length() <= 64 * 1024) { "pack.json exceeds 64 KiB" }
            val metadata = PackFormat.metadata(metadataFile.readString("UTF-8"))
            val icon = temporary.child(metadata.icon)
            require(icon.exists() && !icon.isDirectory && icon.length() <= 4 * 1024 * 1024) { "Missing icon or icon exceeds 4 MiB" }
            val dimensions = PngDimensions.read(icon)
            require(dimensions.first <= 1024 && dimensions.second <= 1024) { "Pack icon must be at most 1024 × 1024" }
            validateJson(temporary)
            require(metadata.textureScales.keys.all { temporary.child(it).exists() }) { "textureScales must refer to images in this pack" }
            val destination = root.child(id)
            if (destination.exists()) {
                destination.child(".removed").delete()
            } else {
                require(temporary.file().renameTo(destination.file())) { "Cannot install pack" }
            }
            return InstalledAssetPack(id, metadata, destination)
        } finally {
            temporary.deleteDirectory()
            archive.delete()
        }
    }

    private fun validateJson(directory: FileHandle) {
        directory.list().forEach { file ->
            if (file.isDirectory) {
                validateJson(file)
            } else if (file.extension() == "json") {
                require(file.length() <= 16 * 1024 * 1024) { "JSON file exceeds 16 MiB: ${file.name()}" }
                Json.parseToJsonElement(file.readString("UTF-8"))
            }
        }
    }

    override fun saveSelection(ids: List<String>) {
        val available = installed().map { it.id }.toSet()
        require(ids.distinct().size == ids.size && ids.all { it in available }) { "Selected pack is missing" }
        preferences.setPreference(SELECTION_KEY, ids.joinToString(","))
        preferences.setPreference(ERROR_KEY, null)
    }

    override fun remove(id: String) {
        val pack = installed().firstOrNull { it.id == id } ?: return
        saveSelection(pendingIds - id)
        if (id in runningIds) {
            pack.directory.child(".removed").writeString("", false)
        } else {
            pack.directory.deleteDirectory()
        }
    }

    override fun disableAfterFailure(message: String) {
        preferences.setPreference(SELECTION_KEY, "")
        preferences.setPreference(ERROR_KEY, message)
    }

    companion object {
        private const val SELECTION_KEY = "asset_packs"
        private const val ERROR_KEY = "asset_packs_error"
        private val logger = Logger.withTag("AssetPacks")
    }
}
