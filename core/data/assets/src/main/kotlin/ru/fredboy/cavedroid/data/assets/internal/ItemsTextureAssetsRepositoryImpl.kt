package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.Texture
import ru.fredboy.cavedroid.domain.assets.repository.ItemsTextureAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ItemsTextureAssetsRepositoryImpl @Inject constructor() : ItemsTextureAssetsRepository() {

    private val itemTexturesCache = HashMap<String, Texture>()

    override fun getItemTexture(textureName: String): Texture = resolveTexture(textureName, ITEMS_TEXTURES_PATH, itemTexturesCache)

    override fun initialize() {
        // no-op
    }

    override fun dispose() {
        super.dispose()
        itemTexturesCache.clear()
    }

    companion object {
        private const val ITEMS_TEXTURES_PATH = "textures/textures/items"
    }
}
