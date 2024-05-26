package ru.fredboy.cavedroid.domain.assets.repository

import ru.fredboy.cavedroid.domain.assets.model.MobSprite

abstract class MobAssetsRepository : AssetsRepository() {

    abstract fun getPlayerSprites(): MobSprite.Player

    abstract fun getPigSprites(): MobSprite.Pig

}