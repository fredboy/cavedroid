package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.Texture
import ru.fredboy.cavedroid.domain.assets.repository.BlockTextureAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BlockTextureAssetsRepositoryImpl @Inject constructor() : BlockTextureAssetsRepository() {

    private val blockTexturesCache = HashMap<String, Texture>()

    override fun getBlockTexture(textureName: String): Texture = resolveTexture(textureName, BLOCKS_TEXTURES_PATH, blockTexturesCache)

    override fun initialize() {
        // no-op
    }

    override fun dispose() {
        super.dispose()
        blockTexturesCache.clear()
    }

    companion object {
        private const val BLOCKS_TEXTURES_PATH = "textures/textures/blocks"
    }
}
