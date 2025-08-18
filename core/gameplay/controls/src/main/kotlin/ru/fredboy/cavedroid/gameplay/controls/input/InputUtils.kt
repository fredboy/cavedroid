package ru.fredboy.cavedroid.gameplay.controls.input

import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.gameplay.controls.input.action.MouseInputAction

fun MouseInputAction.isInsideHotbar(
    gameContextRepository: GameContextRepository,
    getTextureRegionByNameUseCase: GetTextureRegionByNameUseCase,
): Boolean {
    val hotbar = requireNotNull(getTextureRegionByNameUseCase["hotbar"])

    return this.screenY <= hotbar.regionHeight &&
        this.screenX >= gameContextRepository.getCameraContext().viewport.width / 2 - hotbar.regionWidth / 2 &&
        this.screenX <= gameContextRepository.getCameraContext().viewport.width / 2 + hotbar.regionWidth / 2
}

fun isInsideWindow(
    gameContextRepository: GameContextRepository,
    action: MouseInputAction,
    windowTexture: TextureRegion,
): Boolean = action.screenY > gameContextRepository.getCameraContext().viewport.height / 2 - windowTexture.regionHeight / 2 &&
    action.screenY < gameContextRepository.getCameraContext().viewport.height / 2 + windowTexture.regionHeight / 2 &&
    action.screenX > gameContextRepository.getCameraContext().viewport.width / 2 - windowTexture.regionWidth / 2 &&
    action.screenX < gameContextRepository.getCameraContext().viewport.width / 2 + windowTexture.regionWidth / 2
