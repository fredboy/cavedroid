package ru.fredboy.cavedroid.domain.assets.usecase

import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.domain.assets.model.MobSprite
import javax.inject.Inject

@Reusable
class GetPlayerSpritesUseCase @Inject constructor(
    private val gameAssetsHolder: GameAssetsHolder,
) {

    operator fun invoke(): MobSprite.Player = gameAssetsHolder.getPlayerSprites()
}
