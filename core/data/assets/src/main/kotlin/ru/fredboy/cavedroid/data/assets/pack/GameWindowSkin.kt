package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import ru.fredboy.cavedroid.domain.assets.pack.AssetTextureLoader
import javax.inject.Inject
import javax.inject.Singleton

/** Scene2D styles for game windows, independent of the menu skin and its fonts. */
@Singleton
class GameWindowSkin @Inject constructor(
    resolver: AssetResolver,
    textures: AssetTextureLoader,
) : PackSkin(resolver, textures, "skin/gameplay") {
    init {
        getAll(Texture::class.java).values().forEach { texture ->
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }
        getAll(BitmapFont::class.java).values().forEach { font ->
            font.regions.forEach { region ->
                region.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            }
        }
    }
}
