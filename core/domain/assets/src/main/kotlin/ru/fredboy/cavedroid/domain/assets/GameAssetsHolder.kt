package ru.fredboy.cavedroid.domain.assets

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.model.MobSprite
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import ru.fredboy.cavedroid.domain.assets.repository.*
import javax.inject.Inject

@Reusable
class GameAssetsHolder @Inject constructor(
    private val blockAssetsRepository: BlockAssetsRepository,
    private val blockDamageAssetsRepository: BlockDamageAssetsRepository,
    private val fontAssetsRepository: FontAssetsRepository,
    private val itemsAssetsRepository: ItemsAssetsRepository,
    private val mobAssetsRepository: MobAssetsRepository,
    private val textureRegionsAssetsRepository: TextureRegionsAssetsRepository,
    private val touchButtonsAssetsRepository: TouchButtonsAssetsRepository
) {

    private val repositories = sequenceOf(
        blockAssetsRepository,
        blockDamageAssetsRepository,
        fontAssetsRepository,
        itemsAssetsRepository,
        mobAssetsRepository,
        textureRegionsAssetsRepository,
        touchButtonsAssetsRepository
    )

    fun initializeRepository() {
        repositories.forEach(AssetsRepository::initialize)
    }

    fun dispose() {
        repositories.forEach(AssetsRepository::dispose)
    }

    fun getBlockTexture(textureName: String): Texture {
        return blockAssetsRepository.getBlockTexture(textureName)
    }

    fun getItemTexture(textureName: String): Texture {
        return itemsAssetsRepository.getItemTexture(textureName)
    }

    fun getBlockDamageFrameCount(): Int {
        return blockDamageAssetsRepository.damageStages
    }

    fun getBlockDamageSprite(stage: Int): Sprite {
        return blockDamageAssetsRepository.getBlockDamageSprite(stage)
    }

    fun getStringWidth(string: String): Float {
        return fontAssetsRepository.getStringWidth(string)
    }

    fun getStringHeight(string: String): Float {
        return fontAssetsRepository.getStringHeight(string)
    }

    fun getFont(): BitmapFont {
        return fontAssetsRepository.getFont()
    }

    fun getPlayerSprites(): MobSprite.Player {
        return mobAssetsRepository.getPlayerSprites()
    }

    fun getPigSprites(): MobSprite.Pig {
        return mobAssetsRepository.getPigSprites()
    }

    fun getTouchButtons(): Map<String, TouchButton> {
        return touchButtonsAssetsRepository.getTouchButtons()
    }

    fun getTextureRegionByName(name: String): TextureRegion? {
        return textureRegionsAssetsRepository.getTextureRegionByName(name)
    }

}
