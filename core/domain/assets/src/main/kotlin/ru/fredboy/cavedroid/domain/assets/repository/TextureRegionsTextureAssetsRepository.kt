package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.TextureRegion

abstract class TextureRegionsTextureAssetsRepository : TextureAssetsRepository() {

    abstract fun getTextureRegionByName(name: String): TextureRegion?
}
