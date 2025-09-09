package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.domain.assets.repository.MobTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsTextureAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MobTextureAssetsRepositoryImpl @Inject constructor(
    private val textureRegionsAssetsRepository: TextureRegionsTextureAssetsRepository,
) : MobTextureAssetsRepository() {

    private val mobTexturesCache = mutableMapOf<String, MutableMap<String, Texture>>()
    private var playerCursorSprite: Sprite? = null

    private var crosshairSprite: Sprite? = null

    override fun getMobTexture(mobName: String, textureName: String): Texture {
        val cache = mobTexturesCache[mobName] ?: mutableMapOf<String, Texture>().also { mobTexturesCache[mobName] = it }

        return resolveTexture(
            textureName = textureName,
            lookupPath = "textures/mobs/$mobName",
            cache = cache,
        )
    }

    override fun getPlayerCursorSprite(): Sprite {
        return requireNotNull(playerCursorSprite)
    }

    override fun getCrosshairSprite(): Sprite {
        return requireNotNull(crosshairSprite)
    }

    override fun initialize() {
        playerCursorSprite = textureRegionsAssetsRepository.getTextureRegionByName(CURSOR_KEY)?.let {
            Sprite(it).apply {
                setSize(1f, 1f)
            }
        }

        crosshairSprite = textureRegionsAssetsRepository.getTextureRegionByName(CROSSHAIR_KEY)?.let {
            Sprite(it).apply {
                setSize(0.5f, 0.5f)
            }
        }
    }

    override fun dispose() {
        super.dispose()
        playerCursorSprite = null
        mobTexturesCache.clear()
    }

    companion object {
        private const val CURSOR_KEY = "cursor"
        private const val CROSSHAIR_KEY = "crosshair"
    }
}
