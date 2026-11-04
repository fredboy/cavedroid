package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.files.FileHandle
import java.io.DataInputStream

internal object PngDimensions {
    fun read(file: FileHandle): Pair<Int, Int> = DataInputStream(file.read()).use { input ->
        require(input.readLong() == -8552249625308161526L && input.readInt() == 13 && input.readInt() == 0x49484452) {
            "Expected PNG image: ${file.path()}"
        }
        val width = input.readInt()
        val height = input.readInt()
        require(width > 0 && height > 0) { "Invalid PNG dimensions: ${file.path()}" }
        width to height
    }
}
