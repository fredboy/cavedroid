package ru.fredboy.cavedroid.domain.assets.usecase

import com.badlogic.gdx.graphics.g2d.TextureRegion
import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import javax.inject.Inject

@Reusable
class GetTextureRegionByNameUseCase @Inject constructor(
    private val gameAssetsHolder: GameAssetsHolder,
) {

    operator fun get(name: String): TextureRegion? = gameAssetsHolder.getTextureRegionByName(name)
}
