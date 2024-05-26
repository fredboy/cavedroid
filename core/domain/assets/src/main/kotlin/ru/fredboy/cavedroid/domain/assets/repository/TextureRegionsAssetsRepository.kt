package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.TextureRegion

abstract class TextureRegionsAssetsRepository : AssetsRepository() {

    abstract fun getTextureRegionByName(name: String): TextureRegion?

}
