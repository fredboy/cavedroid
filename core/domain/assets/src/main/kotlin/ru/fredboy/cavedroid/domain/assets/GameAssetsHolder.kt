package ru.fredboy.cavedroid.domain.assets

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import ru.fredboy.cavedroid.domain.assets.repository.AssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockActionSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockDamageTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.BlockTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.DropSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.FoodSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.ItemsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.MobSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.MobTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.TouchButtonsTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.UiSoundAssetsRepository
import ru.fredboy.cavedroid.domain.assets.repository.WearableTextureAssetsRepository
import javax.inject.Inject

@Reusable
class GameAssetsHolder @Inject constructor(
    private val blockAssetsRepository: BlockTextureAssetsRepository,
    private val blockDamageAssetsRepository: BlockDamageTextureAssetsRepository,
    private val fontAssetsRepository: FontTextureAssetsRepository,
    private val itemsAssetsRepository: ItemsTextureAssetsRepository,
    private val mobAssetsRepository: MobTextureAssetsRepository,
    private val textureRegionsAssetsRepository: TextureRegionsTextureAssetsRepository,
    private val touchButtonsAssetsRepository: TouchButtonsTextureAssetsRepository,
    private val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepositoryTexture,
    private val stepsSoundAssetsRepository: StepsSoundAssetsRepository,
    private val uiSoundAssetsRepository: UiSoundAssetsRepository,
    private val mobSoundAssetsRepository: MobSoundAssetsRepository,
    private val dropSoundAssetsRepository: DropSoundAssetsRepository,
    private val foodSoundAssetsRepository: FoodSoundAssetsRepository,
    private val blockActionSoundAssetsRepository: BlockActionSoundAssetsRepository,
    private val wearableTextureAssetsRepository: WearableTextureAssetsRepository,
) {

    private val repositories = sequenceOf(
        blockAssetsRepository,
        blockDamageAssetsRepository,
        fontAssetsRepository,
        itemsAssetsRepository,
        textureRegionsAssetsRepository,
        touchButtonsAssetsRepository,
        mobAssetsRepository,
        environmentTextureRegionsRepository,
        stepsSoundAssetsRepository,
        uiSoundAssetsRepository,
        mobSoundAssetsRepository,
        dropSoundAssetsRepository,
        foodSoundAssetsRepository,
        blockActionSoundAssetsRepository,
        wearableTextureAssetsRepository,
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

    fun getMobTexture(mobName: String, textureName: String): Texture {
        return mobAssetsRepository.getMobTexture(mobName, textureName)
    }

    fun getTouchButtons(): Map<String, TouchButton> = touchButtonsAssetsRepository.getTouchButtons()

    fun getTextureRegionByName(name: String): TextureRegion? = textureRegionsAssetsRepository.getTextureRegionByName(name)
}
