package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.assets.repository.WearableTextureAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableTextureAssetsRepositoryImpl @Inject constructor() : WearableTextureAssetsRepository() {

    private val frontSpritesMap = mutableMapOf<String, Sprite?>()

    override fun getFrontSprite(material: String, slot: Int): Sprite? {
        val key = "$material/$slot"
        return if (frontSpritesMap.containsKey(key)) {
            frontSpritesMap[key]
        } else {
            try {
                flippedSprite(loadTexture("textures/equipment/$key.png"))
            } catch (e: Exception) {
                Gdx.app.error(TAG, "Couldn't load wearable sprite", e)
                null
            }?.also { sprite ->
                frontSpritesMap[key] = sprite
            }
        }
    }

    override fun initialize() {
        // no-op
    }

    override fun dispose() {
        super.dispose()
        frontSpritesMap.clear()
    }

    companion object {
        private const val TAG = "WearableTextureAssetsRepositoryImpl"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
