package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.Sprite

abstract class BlockDamageTextureAssetsRepository : TextureAssetsRepository() {

    abstract val damageStages: Int

    abstract fun getBlockDamageSprite(stage: Int): Sprite
}
