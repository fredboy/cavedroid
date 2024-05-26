package ru.fredboy.cavedroid.domain.assets.usecase

import com.badlogic.gdx.graphics.Texture
import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import javax.inject.Inject

@Reusable
class GetBlockTextureUseCase @Inject constructor(
    private val gameAssetsHolder: GameAssetsHolder
) {

    operator fun get(name: String): Texture {
        return gameAssetsHolder.getBlockTexture(name)
    }

}