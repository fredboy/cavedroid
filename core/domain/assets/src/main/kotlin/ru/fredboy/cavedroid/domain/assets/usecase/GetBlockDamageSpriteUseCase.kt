package ru.fredboy.cavedroid.domain.assets.usecase

import com.badlogic.gdx.graphics.g2d.Sprite
import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import javax.inject.Inject

@Reusable
class GetBlockDamageSpriteUseCase @Inject constructor(
    private val gameAssetsHolder: GameAssetsHolder,
) {

    operator fun get(stage: Int): Sprite {
        return gameAssetsHolder.getBlockDamageSprite(stage)
    }
}
