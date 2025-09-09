package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.util.*

abstract class TextureAssetsRepository : AssetsRepository {

    protected val loadedTextures = LinkedList<Texture>()

    protected fun loadTexture(path: String): Texture = Texture(Gdx.files.internal(path)).also { texture ->
        loadedTextures.add(texture)
    }

    protected fun flippedRegion(
        texture: Texture,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): TextureRegion = TextureRegion(texture, x, y + height, width, -height)

    protected fun flippedSprite(
        texture: Texture,
    ): Sprite = Sprite(texture).apply { flip(false, true) }

    protected fun flippedSprite(
        texture: Texture,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): Sprite = Sprite(flippedRegion(texture, x, y, width, height))

    protected fun resolveTexture(
        textureName: String,
        lookupPath: String,
        cache: MutableMap<String, Texture>,
    ): Texture {
        val cached = cache[textureName]

        if (cached != null) {
            return cached
        }

        val texture = loadTexture("$lookupPath/$textureName.png")
        cache[textureName] = texture

        return texture
    }

    override fun dispose() {
        loadedTextures.forEach(Texture::dispose)
        loadedTextures.clear()
    }
}
