package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

abstract class EnvironmentTextureRegionsRepositoryTexture : TextureAssetsRepository() {

    abstract fun getSunSprite(): Sprite

    abstract fun getMoonPhaseSprite(phase: Int): Sprite

    abstract fun getRainTexture(): Texture

    abstract fun getSnowTexture(): Texture

    abstract fun getMoonPhasesCount(): Int
}
