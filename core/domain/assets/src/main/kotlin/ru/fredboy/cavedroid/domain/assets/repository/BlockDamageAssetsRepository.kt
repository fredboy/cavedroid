package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.Sprite

abstract class BlockDamageAssetsRepository : AssetsRepository() {

    abstract val damageStages: Int

    abstract fun getBlockDamageSprite(stage: Int): Sprite

}
