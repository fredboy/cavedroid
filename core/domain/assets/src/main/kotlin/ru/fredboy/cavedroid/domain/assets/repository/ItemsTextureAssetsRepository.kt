package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.Texture

abstract class ItemsTextureAssetsRepository : TextureAssetsRepository() {

    abstract fun getItemTexture(textureName: String): Texture
}
