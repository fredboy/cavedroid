package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.I18NBundle

abstract class FontAssetsRepository : AssetsRepository() {

    abstract fun getMenuLocalizationBundle(): I18NBundle

    abstract fun getStringWidth(string: String): Float

    abstract fun getStringHeight(string: String): Float

    abstract fun getFont(): BitmapFont
}
