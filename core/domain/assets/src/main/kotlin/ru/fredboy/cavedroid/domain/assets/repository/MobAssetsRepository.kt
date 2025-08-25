package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

abstract class MobAssetsRepository : AssetsRepository() {

    abstract fun getMobTexture(mobName: String, textureName: String): Texture

    abstract fun getPlayerCursorSprite(): Sprite

    abstract fun getCrosshairSprite(): Sprite
}
