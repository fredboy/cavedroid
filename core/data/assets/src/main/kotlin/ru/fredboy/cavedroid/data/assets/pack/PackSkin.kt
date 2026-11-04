package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import ru.fredboy.cavedroid.domain.assets.pack.AssetTextureLoader

class PackSkin(private val resolver: AssetResolver, private val textures: AssetTextureLoader) : Skin() {
    init {
        try {
            val atlasFile = resolver.handle("skin/skin.atlas")
            val data = TextureAtlas.TextureAtlasData(atlasFile, atlasFile.parent(), false)
            data.pages.forEach { page -> page.texture = textures.load(page.textureFile.path()) }
            val atlas = TextureAtlas(data)
            // The no-arg Skin constructor has no atlas to dispose; register page textures explicitly.
            addRegions(atlas)
            atlas.textures.forEachIndexed { index, texture -> add("pack-page-$index", texture, Texture::class.java) }
            load(resolver.handle("skin/skin.json"))
        } catch (exception: Exception) {
            dispose()
            throw exception
        }
    }

    override fun getJsonLoader(skinFile: FileHandle): Json = super.getJsonLoader(skinFile).apply {
        setSerializer(
            BitmapFont::class.java,
            object : Json.ReadOnlySerializer<BitmapFont>() {
                override fun read(json: Json, jsonData: JsonValue, type: Class<*>): BitmapFont {
                    val path = skinFile.parent().child(jsonData.getString("file")).path()
                    return textures.loadFont(path, jsonData.getBoolean("flip", false)).apply {
                        data.markupEnabled = jsonData.getBoolean("markupEnabled", false)
                        setUseIntegerPositions(jsonData.getBoolean("useIntegerPositions", true))
                        val scaledSize = jsonData.getFloat("scaledSize", -1f)
                        if (scaledSize != -1f) data.setScale(scaledSize / capHeight)
                    }
                }
            },
        )
    }
}
