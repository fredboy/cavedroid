package ru.fredboy.cavedroid.domain.assets.pack

import com.badlogic.gdx.files.FileHandle

interface AssetResolver {
    /** A virtual handle whose relative references and JSON contents use the same stack. */
    fun handle(path: String): FileHandle

    /** A real backend handle, needed by native audio decoders. */
    fun resolve(path: String): FileHandle
    fun textureScale(path: String, width: Int, height: Int): Int
    val hasOverrides: Boolean
}
