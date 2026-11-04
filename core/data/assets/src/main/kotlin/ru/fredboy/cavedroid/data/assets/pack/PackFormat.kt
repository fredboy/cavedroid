package ru.fredboy.cavedroid.data.assets.pack

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackFormat
import ru.fredboy.cavedroid.domain.assets.pack.PackMetadata

object PackFormat {
    const val MAX_ARCHIVE_BYTES = 256L * 1024 * 1024
    const val MAX_EXTRACTED_BYTES = 1024L * 1024 * 1024
    const val MAX_ENTRIES = 20_000

    fun path(value: String): String {
        require(
            value.isNotEmpty() && value.length <= 1024 && !value.contains('\\') && !value.contains(':') &&
                value.none { it.code < 32 } && value.split('/').none { it.isEmpty() || it == "." || it == ".." },
        ) { "Invalid asset path: $value" }
        return value
    }

    fun metadata(text: String): PackMetadata {
        val root = Json.parseToJsonElement(text) as? JsonObject ?: error("pack.json must be an object")
        fun JsonObject.string(key: String): String = (get(key) as? JsonPrimitive)
            ?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() } ?: error("Missing or invalid metadata: $key")
        val format = (root["formatVersion"] as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull
        require(format == AssetPackFormat.VERSION) {
            "Unsupported asset-pack format version: $format (supported: ${AssetPackFormat.VERSION})"
        }
        val scales = root["textureScales"]?.let { element ->
            val obj = element as? JsonObject ?: error("textureScales must be an object")
            obj.mapValues { (key, value) ->
                path(key)
                val scale = (value as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull
                require(scale != null && scale > 0) { "Invalid texture scale: $key" }
                scale
            }
        } ?: emptyMap()
        return PackMetadata(
            AssetPackFormat.VERSION,
            root.string("name"),
            root.string("version"),
            root.string("author"),
            root.string("license"),
            path(root.string("icon")),
            scales,
        )
    }

    fun merge(base: JsonElement, override: JsonElement): JsonElement {
        if (base !is JsonObject || override !is JsonObject) return override
        val merged = base.toMutableMap()
        override.forEach { (key, value) -> merged[key] = merged[key]?.let { merge(it, value) } ?: value }
        return JsonObject(merged)
    }

    fun scale(width: Int, height: Int, baseWidth: Int?, baseHeight: Int?, explicit: Int?): Int {
        require(width > 0 && height > 0) { "Invalid image dimensions" }
        if (explicit != null) {
            require(explicit > 0 && width % explicit == 0 && height % explicit == 0) { "Invalid explicit texture scale" }
            return explicit
        }
        require(baseWidth != null && baseHeight != null) { "New textures require textureScales metadata" }
        require(width % baseWidth == 0 && height % baseHeight == 0 && width / baseWidth == height / baseHeight) {
            "Changed texture layouts require textureScales metadata"
        }
        return width / baseWidth
    }
}
