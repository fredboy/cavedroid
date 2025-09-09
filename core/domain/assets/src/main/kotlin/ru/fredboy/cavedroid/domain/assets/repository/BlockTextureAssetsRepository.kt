package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.Texture

abstract class BlockTextureAssetsRepository : TextureAssetsRepository() {

    abstract fun getBlockTexture(textureName: String): Texture
}
