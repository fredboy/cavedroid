package ru.fredboy.cavedroid.ux.controls.input

import com.badlogic.gdx.graphics.g2d.TextureRegion
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.ux.controls.input.action.MouseInputAction

fun MouseInputAction.isInsideHotbar(getTextureRegionByNameUseCase: GetTextureRegionByNameUseCase): Boolean {
    val hotbar = requireNotNull(getTextureRegionByNameUseCase["hotbar"])

    return this.screenY <= hotbar.regionHeight &&
            this.screenX >= this.cameraViewport.width / 2 - hotbar.regionWidth / 2 &&
            this.screenX <= this.cameraViewport.width / 2 + hotbar.regionWidth / 2
}

fun isInsideWindow(action: MouseInputAction, windowTexture: TextureRegion): Boolean {
    return action.screenY > action.cameraViewport.height / 2 - windowTexture.regionHeight / 2 &&
            action.screenY < action.cameraViewport.height / 2 + windowTexture.regionHeight / 2 &&
            action.screenX > action.cameraViewport.width / 2 - windowTexture.regionWidth / 2 &&
            action.screenX < action.cameraViewport.width / 2 + windowTexture.regionWidth / 2
}
