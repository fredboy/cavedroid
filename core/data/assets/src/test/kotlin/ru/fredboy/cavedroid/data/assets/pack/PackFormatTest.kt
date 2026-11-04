package ru.fredboy.cavedroid.data.assets.pack

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PackFormatTest {
    private val metadata = """{"formatVersion":1,"name":"Example","version":"1.0","author":"Author","license":"CC0-1.0","icon":"icon.png"}"""

    @Test
    fun `recursive merge preserves existing fields and key order`() {
        val base = Json.parseToJsonElement("""{"blocks":{"stone":{"hp":10,"texture":"stone"}},"items":{}}""")
        val patch = Json.parseToJsonElement("""{"blocks":{"stone":{"hp":25},"new":{"hp":5}},"extra":true}""")
        val merged = PackFormat.merge(base, patch)
        assertEquals(Json.parseToJsonElement("""{"blocks":{"stone":{"hp":25,"texture":"stone"},"new":{"hp":5}},"items":{},"extra":true}"""), merged)
        assertEquals(listOf("blocks", "items", "extra"), merged.jsonObject.keys.toList())
        assertEquals(listOf("stone", "new"), merged.jsonObject.getValue("blocks").jsonObject.keys.toList())
    }

    @Test
    fun `arrays and explicit null replace lower values`() {
        val result = PackFormat.merge(
            Json.parseToJsonElement("""{"recipes":[1,2],"texture":"stone"}"""),
            Json.parseToJsonElement("""{"recipes":[3],"texture":null}"""),
        )
        assertEquals(Json.parseToJsonElement("""{"recipes":[3],"texture":null}"""), result)
    }

    @Test
    fun `pack format version is independent of pack release version`() {
        assertEquals(1, PackFormat.metadata(metadata).formatVersion)
        val pack = PackFormat.metadata(metadata.replace("Example", "Another pack").replace("1.0", "2026-custom"))
        assertEquals(1, pack.formatVersion)
        assertEquals("2026-custom", pack.version)
    }

    @Test
    fun `missing malformed and unsupported format versions are rejected`() {
        listOf("0", "-1", "2", "1.5", "\"1\"", "null", "{}", "[]", "true").forEach { version ->
            assertThrows(IllegalArgumentException::class.java) {
                PackFormat.metadata(metadata.replace("\"formatVersion\":1", "\"formatVersion\":$version"))
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            PackFormat.metadata(metadata.replace("\"formatVersion\":1,", ""))
        }
    }

    @Test
    fun `metadata requires versioned format and relative icon`() {
        assertEquals("Example", PackFormat.metadata(metadata).name)
        assertThrows(IllegalArgumentException::class.java) { PackFormat.metadata(metadata.replace(""""formatVersion":1""", """"formatVersion":2""")) }
        assertThrows(IllegalArgumentException::class.java) { PackFormat.metadata(metadata.replace("icon.png", "../icon.png")) }
    }

    @Test
    fun `unsafe paths are rejected rather than normalized`() {
        listOf("../a", "/a", "a/../b", "a//b", "a/./b", "C:/a", "a\\b", "a\u0000b", "").forEach {
            assertThrows(IllegalArgumentException::class.java, { PackFormat.path(it) }, it)
        }
        assertEquals("textures/textures/blocks/stone.png", PackFormat.path("textures/textures/blocks/stone.png"))
    }

    @Test
    fun `resolution scales per image and preserves sheet geometry`() {
        assertEquals(1, PackFormat.scale(16, 16, 16, 16, null))
        assertEquals(2, PackFormat.scale(32, 32, 16, 16, null))
        assertEquals(4, PackFormat.scale(64, 640, 16, 160, null))
        assertEquals(2, PackFormat.scale(352, 332, 176, 166, null))
        assertEquals(4, PackFormat.scale(64, 128, null, null, 4))
        assertThrows(IllegalArgumentException::class.java) { PackFormat.scale(32, 64, 16, 16, null) }
        assertThrows(IllegalArgumentException::class.java) { PackFormat.scale(32, 32, null, null, null) }
        assertThrows(IllegalArgumentException::class.java) { PackFormat.scale(31, 32, 16, 16, 2) }
    }
}
