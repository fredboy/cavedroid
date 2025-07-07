package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.assets.model.MobSprite

abstract class MobAssetsRepository : AssetsRepository() {

    abstract fun getPlayerSprites(): MobSprite.Player

    abstract fun getPigSprites(): MobSprite.Pig

    abstract fun getPlayerCursorSprite(): Sprite
}
