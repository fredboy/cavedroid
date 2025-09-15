package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.Sprite

abstract class WearableTextureAssetsRepository : TextureAssetsRepository() {

    abstract fun getFrontSprite(material: String, slot: Int): Sprite?
}
