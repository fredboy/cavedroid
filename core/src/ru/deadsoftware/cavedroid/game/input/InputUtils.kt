package ru.deadsoftware.cavedroid.game.input

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.misc.Assets

fun isInsideHotbar(action: MouseInputAction): Boolean {
    val hotbar = requireNotNull(Assets.textureRegions["hotbar"])

    return action.screenY <= hotbar.regionHeight &&
            action.screenX >= action.cameraViewport.width / 2 - hotbar.regionWidth / 2 &&
            action.screenX <= action.cameraViewport.width / 2 + hotbar.regionWidth / 2
}

fun isInsideWindow(action: MouseInputAction, windowTexture: TextureRegion): Boolean {
    return action.screenY > action.cameraViewport.height / 2 - windowTexture.regionHeight / 2 &&
            action.screenY < action.cameraViewport.height / 2 + windowTexture.regionHeight / 2 &&
            action.screenX > action.cameraViewport.width / 2 - windowTexture.regionWidth / 2 &&
            action.screenX < action.cameraViewport.width / 2 + windowTexture.regionWidth / 2
}
