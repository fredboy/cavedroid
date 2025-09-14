package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.Sprite

abstract class WearableTextureAssetsRepository : TextureAssetsRepository() {

    abstract fun getSideSprite(name: String): Sprite

    abstract fun getFrontSprite(name: String): Sprite
}
