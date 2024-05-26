package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.Texture

abstract class BlockAssetsRepository : AssetsRepository() {

    abstract fun getBlockTexture(textureName: String): Texture

}