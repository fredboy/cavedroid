package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.graphics.g2d.Sprite

abstract class EnvironmentTextureRegionsRepositoryTexture : TextureAssetsRepository() {

    abstract fun getSunSprite(): Sprite

    abstract fun getMoonPhaseSprite(phase: Int): Sprite

    abstract fun getRainSprite(frame: Int): Sprite

    abstract fun getSnowSprite(frame: Int): Sprite

    abstract fun getMoonPhasesCount(): Int
}
