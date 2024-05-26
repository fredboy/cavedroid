package ru.fredboy.cavedroid.domain.assets.usecase

import com.badlogic.gdx.graphics.g2d.BitmapFont
import dagger.Reusable
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import javax.inject.Inject

@Reusable
class GetFontUseCase @Inject constructor(
    private val gameAssetsHolder: GameAssetsHolder,
) {

    operator fun invoke(): BitmapFont {
        return gameAssetsHolder.getFont()
    }

}
