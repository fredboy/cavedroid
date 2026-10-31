package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import ru.fredboy.cavedroid.domain.assets.pack.AssetTextureLoader
import java.util.*
import javax.inject.Inject

abstract class TextureAssetsRepository : AssetsRepository {

    @Inject lateinit var assetResolver: AssetResolver

    @Inject lateinit var textureLoader: AssetTextureLoader

    protected val loadedTextures = LinkedList<Texture>()

    protected fun loadTexture(path: String): Texture = textureLoader.load(path).also { texture ->
        loadedTextures.add(texture)
    }

    protected fun flippedRegion(
        texture: Texture,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): TextureRegion {
        require(
            x >= 0 && y >= 0 && width > 0 && height > 0 &&
                x.toLong() + width <= texture.width && y.toLong() + height <= texture.height,
        ) { "Texture region outside logical image bounds: $x,$y $width×$height in ${texture.width}×${texture.height}" }
        return TextureRegion(texture, x, y + height, width, -height)
    }

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
