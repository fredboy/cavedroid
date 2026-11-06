package ru.fredboy.cavedroid.gdx.game.window.waystone

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.data.assets.pack.GameWindowSkin
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockTextureUseCase
import ru.fredboy.cavedroid.game.window.GameWindowModel
import ru.fredboy.cavedroid.game.window.waystone.WaystoneWindowModel
import ru.fredboy.cavedroid.gdx.game.window.BindGameWindow
import ru.fredboy.cavedroid.gdx.game.window.GameWindowFactory
import ru.fredboy.cavedroid.gdx.game.window.GameWindowView
import javax.inject.Inject

@GameScope
@BindGameWindow(stringKey = WaystoneWindowModel.KEY)
class WaystoneWindowFactory @Inject constructor(
    private val skin: GameWindowSkin,
    private val fonts: FontTextureAssetsRepository,
    private val inlineTextInput: InlineTextInput,
    private val keyboard: SoftKeyboardObserver,
    private val blockTextures: GetBlockTextureUseCase,
) : GameWindowFactory {
    override fun create(model: GameWindowModel, touch: Boolean, close: () -> Unit): GameWindowView = WaystoneWindowView(
        model = model as WaystoneWindowModel,
        skin = skin,
        touch = touch,
        strings = fonts.getMenuLocalizationBundle(),
        title = fonts.getItemLocalizationBundle()["waystone"],
        inlineTextInput = inlineTextInput,
        keyboard = keyboard,
        close = close,
        icon = TextureRegionDrawable(TextureRegion(blockTextures["waystone"])),
    )
}
