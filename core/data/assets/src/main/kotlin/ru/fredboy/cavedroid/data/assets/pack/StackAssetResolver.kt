package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import kotlinx.serialization.json.Json
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackRepository
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StackAssetResolver @Inject constructor(private val packs: AssetPackRepository) : AssetResolver {
    private val stack by lazy {
        val installed = packs.installed().associateBy { it.id }
        packs.runningIds.map { installed[it] ?: error("Enabled pack is missing: $it") }
    }
    private val mergedJson = HashMap<String, ByteArray>()
    override val hasOverrides get() = packs.runningIds.isNotEmpty()

    override fun resolve(path: String): FileHandle {
        PackFormat.path(path)
        return stack.firstOrNull { it.directory.child(path).exists() }?.directory?.child(path) ?: Gdx.files.internal(path)
    }

    override fun handle(path: String): FileHandle {
        if (path.isNotEmpty()) PackFormat.path(path)
        return if (hasOverrides) LayeredHandle(path) else Gdx.files.internal(path)
    }

    override fun textureScale(path: String, width: Int, height: Int): Int {
        val pack = stack.firstOrNull { it.directory.child(path).exists() } ?: return 1
        val bundled = Gdx.files.internal(path)
        val dimensions = if (bundled.exists() && bundled.extension() == "png") PngDimensions.read(bundled) else null
        return try {
            PackFormat.scale(width, height, dimensions?.first, dimensions?.second, pack.metadata.textureScales[path])
        } catch (exception: IllegalArgumentException) {
            throw IllegalArgumentException("${pack.metadata.name}: $path: ${exception.message}", exception)
        }
    }

    private fun json(path: String): ByteArray = mergedJson.getOrPut(path) {
        val files = listOf(Gdx.files.internal(path)) + stack.asReversed().map { it.directory.child(path) }
        files.filter { it.exists() }.map { Json.parseToJsonElement(it.readString("UTF-8")) }
            .reduce(PackFormat::merge).toString().toByteArray(Charsets.UTF_8)
    }

    private inner class LayeredHandle(private val assetPath: String) : FileHandle(assetPath) {
        override fun read(): InputStream = if (extension() == "json") ByteArrayInputStream(json(assetPath)) else resolve(assetPath).read()
        override fun exists(): Boolean = assetPath.isEmpty() || resolve(assetPath).exists()
        override fun isDirectory(): Boolean = assetPath.isEmpty() || resolve(assetPath).isDirectory
        override fun length(): Long = if (extension() == "json") json(assetPath).size.toLong() else resolve(assetPath).length()
        override fun child(name: String): FileHandle = handle(if (assetPath.isEmpty()) name else "$assetPath/$name")
        override fun parent(): FileHandle = handle(assetPath.substringBeforeLast('/', ""))
        override fun sibling(name: String): FileHandle = parent().child(name)
        override fun list(): kotlin.Array<FileHandle> {
            val dirs = stack.map { it.directory.child(assetPath) } + Gdx.files.internal(assetPath)
            return dirs.filter { it.isDirectory }.flatMap { it.list().map(FileHandle::name) }.distinct().map(::child).toTypedArray()
        }
    }
}
