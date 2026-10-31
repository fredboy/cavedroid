package ru.fredboy.cavedroid.domain.assets.pack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.TextureData
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.BufferUtils
import javax.inject.Inject
import javax.inject.Singleton

/** Keeps all region, font and SpriteBatch coordinates in logical pixels. */
class LogicalTexture(data: TextureData, val pixelScale: Int) : Texture(data) {
    val sourceWidth: Int get() = textureData.width
    val sourceHeight: Int get() = textureData.height
    override fun getWidth(): Int = super.getWidth() / pixelScale.coerceAtLeast(1)
    override fun getHeight(): Int = super.getHeight() / pixelScale.coerceAtLeast(1)
}

@Singleton
class AssetTextureLoader @Inject constructor(private val resolver: AssetResolver) {
    private val allocations = mutableListOf<Texture>()

    fun disposeAll() {
        allocations.forEach(Texture::dispose)
        allocations.clear()
    }

    fun load(path: String): Texture {
        val data = TextureData.Factory.loadFromFile(resolver.resolve(path), null, false)
        if (!data.isPrepared) data.prepare()
        try {
            val scale = resolver.textureScale(path, data.width, data.height)
            val limit = BufferUtils.newIntBuffer(1)
            Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, limit)
            require(data.width <= limit[0] && data.height <= limit[0]) { "Texture exceeds device limit: $path" }
            return LogicalTexture(data, scale).also(allocations::add)
        } catch (exception: Exception) {
            if (data.isPrepared && data.type == TextureData.TextureDataType.Pixmap) data.consumePixmap().dispose()
            throw exception
        }
    }

    fun loadFont(path: String, flip: Boolean): BitmapFont {
        val data = BitmapFont.BitmapFontData(resolver.handle(path), flip)
        val pages = Array<TextureRegion>()
        try {
            data.imagePaths.forEach { pages.add(TextureRegion(load(it))) }
            return BitmapFont(data, pages, true).apply { setOwnsTexture(true) }
        } catch (exception: Exception) {
            pages.forEach { it.texture.dispose() }
            throw exception
        }
    }
}
