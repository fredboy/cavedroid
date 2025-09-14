package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.assets.repository.WearableTextureAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableTextureAssetsRepositoryImpl @Inject constructor() : WearableTextureAssetsRepository() {

    private val sideSpritesMap = mutableMapOf<String, Sprite>()

    private val frontSpritesMap = mutableMapOf<String, Sprite>()

    override fun getSideSprite(name: String): Sprite {
        return sideSpritesMap[name]
            ?: flippedSprite(loadTexture("textures/equipment/${name}_side.png")).also { sprite ->
                sideSpritesMap[name] = sprite
            }
    }

    override fun getFrontSprite(name: String): Sprite {
        return frontSpritesMap[name]
            ?: flippedSprite(loadTexture("textures/equipment/${name}_front.png")).also { sprite ->
                sideSpritesMap[name] = sprite
            }
    }

    override fun initialize() {
        // no-op
    }

    override fun dispose() {
        super.dispose()
    }
}
