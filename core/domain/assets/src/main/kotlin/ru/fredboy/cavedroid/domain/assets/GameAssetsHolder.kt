package ru.fredboy.cavedroid.domain.assets

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.model.MobSprite
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import ru.fredboy.cavedroid.domain.assets.repository.AssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockDamageAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.ItemsAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TouchButtonsAssetsRepository
import javax.inject.Inject

@Reusable
class GameAssetsHolder @Inject constructor(
    private val blockAssetsRepository: BlockAssetsRepository,
    private val blockDamageAssetsRepository: BlockDamageAssetsRepository,
    private val fontAssetsRepository: FontAssetsRepository,
    private val itemsAssetsRepository: ItemsAssetsRepository,
    private val mobAssetsRepository: MobAssetsRepository,
    private val textureRegionsAssetsRepository: TextureRegionsAssetsRepository,
    private val touchButtonsAssetsRepository: TouchButtonsAssetsRepository,
) {

    private val repositories = sequenceOf(
        blockAssetsRepository,
        blockDamageAssetsRepository,
        fontAssetsRepository,
        itemsAssetsRepository,
        textureRegionsAssetsRepository,
        touchButtonsAssetsRepository,
        mobAssetsRepository,
    )

    fun initializeRepository() {
        repositories.forEach(AssetsRepository::initialize)
    }

    fun dispose() {
        repositories.forEach(AssetsRepository::dispose)
    }

    fun getBlockTexture(textureName: String): Texture = blockAssetsRepository.getBlockTexture(textureName)

    fun getItemTexture(textureName: String): Texture = itemsAssetsRepository.getItemTexture(textureName)

    fun getBlockDamageFrameCount(): Int = blockDamageAssetsRepository.damageStages

    fun getBlockDamageSprite(stage: Int): Sprite = blockDamageAssetsRepository.getBlockDamageSprite(stage)

    fun getStringWidth(string: String): Float = fontAssetsRepository.getStringWidth(string)

    fun getStringHeight(string: String): Float = fontAssetsRepository.getStringHeight(string)

    fun getFont(): BitmapFont = fontAssetsRepository.getFont()

    fun getPlayerSprites(): MobSprite.Player = mobAssetsRepository.getPlayerSprites()

    fun getPigSprites(): MobSprite.Pig = mobAssetsRepository.getPigSprites()

    fun getCowSprites(): MobSprite.Cow = mobAssetsRepository.getCowSprites()

    fun getTouchButtons(): Map<String, TouchButton> = touchButtonsAssetsRepository.getTouchButtons()

    fun getTextureRegionByName(name: String): TextureRegion? = textureRegionsAssetsRepository.getTextureRegionByName(name)
}
