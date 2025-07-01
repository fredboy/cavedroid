package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.BitmapFont

abstract class FontAssetsRepository : AssetsRepository() {

    abstract fun getStringWidth(string: String): Float

    abstract fun getStringHeight(string: String): Float

    abstract fun getFont(): BitmapFont
}
