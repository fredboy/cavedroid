package ru.fredboy.cavedroid.gameplay.controls.input

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
