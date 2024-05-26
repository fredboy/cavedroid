package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FontAssetsRepositoryImpl @Inject constructor() : FontAssetsRepository() {

    private val glyphLayout = GlyphLayout()

    private var font: BitmapFont? = null

    override fun getStringWidth(string: String): Float {
        glyphLayout.setText(font, string)
        return glyphLayout.width
    }

    override fun getStringHeight(string: String): Float {
        glyphLayout.setText(font, string)
        return glyphLayout.height
    }

    override fun getFont(): BitmapFont {
        return requireNotNull(font)
    }

    override fun initialize() {
        font = BitmapFont(Gdx.files.internal(FONT_FILE_PATH), true)
            .apply {
                data.setScale(.375f)
                setUseIntegerPositions(false)
            }
    }

    override fun dispose() {
        super.dispose()
        font?.dispose()
    }

    companion object {
        private const val FONT_FILE_PATH = "font.fnt"
        private const val FONT_SCALE = .375f
    }
}