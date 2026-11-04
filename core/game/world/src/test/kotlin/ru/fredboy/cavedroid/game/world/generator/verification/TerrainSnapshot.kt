package ru.fredboy.cavedroid.game.world.generator.verification

import ru.fredboy.cavedroid.data.items.repository.canonicalBlockKey
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

internal data class TerrainCell(val key: String, val state: Int = -1) {
    companion object {
        fun of(block: Block) = TerrainCell(
            canonicalBlockKey(block.params.key),
            when (block) {
                is Block.Water -> block.state
                is Block.Lava -> block.state
                else -> -1
            },
        )
    }
}

internal data class TerrainColumn(val x: Int, val biome: String, val fore: List<TerrainCell>, val back: List<TerrainCell>)

internal data class TerrainSnapshot(val config: String, val columns: List<TerrainColumn>) {
    fun write(output: OutputStream) {
        DataOutputStream(GZIPOutputStream(output)).use { out ->
            out.writeUTF("CaveDroid terrain")
            out.writeInt(1)
            out.writeUTF(config)
            val palette = columns.flatMap { it.fore + it.back }.distinct()
            require(palette.size <= 65536)
            out.writeInt(palette.size)
            palette.forEach {
                out.writeUTF(it.key)
                out.writeInt(it.state)
            }
            val indices = palette.withIndex().associate { it.value to it.index }
            out.writeInt(columns.size)
            columns.forEach { column ->
                out.writeInt(column.x)
                out.writeUTF(column.biome)
                out.writeInt(column.fore.size)
                require(column.fore.size == column.back.size)
                (column.fore + column.back).forEach { out.writeShort(indices.getValue(it)) }
            }
        }
    }

    /** Counts every changed cell and includes the first few exact locations for release diagnostics. */
    fun differences(actual: TerrainSnapshot): String? {
        if (config != actual.config) return "Generator configuration changed: expected $config; actual ${actual.config}"
        if (columns.size != actual.columns.size) return "Column count changed: ${columns.size} -> ${actual.columns.size}"
        var count = 0
        val examples = mutableListOf<String>()
        columns.zip(actual.columns).forEach { (expected, found) ->
            fun mismatch(message: String) {
                count++
                if (examples.size < 12) examples += message
            }
            if (expected.x != found.x) mismatch("column coordinate ${expected.x} -> ${found.x}")
            if (expected.biome != found.biome) mismatch("biome x=${expected.x}: ${expected.biome} -> ${found.biome}")
            for ((layer, pair) in listOf("foreground" to (expected.fore to found.fore), "background" to (expected.back to found.back))) {
                val (before, after) = pair
                if (before.size != after.size) {
                    mismatch("$layer x=${expected.x}: height ${before.size} -> ${after.size}")
                } else {
                    before.indices.forEach { y ->
                        val old = before[y].copy(key = canonicalBlockKey(before[y].key))
                        if (old != after[y]) mismatch("$layer x=${expected.x},y=$y: $old -> ${after[y]}")
                    }
                }
            }
        }
        return if (count == 0) null else "$count mismatches\n${examples.joinToString("\n")}"
    }

    companion object {
        fun read(input: InputStream): TerrainSnapshot = DataInputStream(GZIPInputStream(input)).use { data ->
            require(data.readUTF() == "CaveDroid terrain") { "Invalid terrain snapshot" }
            require(data.readInt() == 1) { "Unsupported terrain snapshot format" }
            val config = data.readUTF()
            val palette = List(data.readInt().also { require(it in 1..65536) }) { TerrainCell(data.readUTF(), data.readInt()) }
            val columns = List(data.readInt().also { require(it in 1..100000) }) {
                val x = data.readInt()
                val biome = data.readUTF()
                val height = data.readInt().also { require(it in 1..4096) }
                TerrainColumn(x, biome, List(height) { palette[data.readUnsignedShort()] }, List(height) { palette[data.readUnsignedShort()] })
            }
            require(data.read() == -1) { "Trailing snapshot data" }
            TerrainSnapshot(config, columns)
        }

        fun config(config: WorldGeneratorConfig): String = config.toString()
    }
}
