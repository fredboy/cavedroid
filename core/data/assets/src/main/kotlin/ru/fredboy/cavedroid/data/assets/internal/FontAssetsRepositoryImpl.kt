package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.I18NBundle
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FontAssetsRepositoryImpl @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : FontAssetsRepository() {

    private val glyphLayout = GlyphLayout()

    private var font: BitmapFont? = null

    private var menuBundle: I18NBundle? = null

    private var itemsBundle: I18NBundle? = null

    override fun getMenuLocalizationBundle(): I18NBundle {
        val locale = applicationContextRepository.getLocale()

        if (locale != menuBundle?.locale) {
            loadLocalization()
            menuBundle?.locale?.let { applicationContextRepository.setLocale(it) }
        }

        return requireNotNull(menuBundle)
    }

    override fun getItemLocalizationBundle(): I18NBundle {
        val locale = applicationContextRepository.getLocale()

        if (locale != itemsBundle?.locale) {
            loadLocalization()
            itemsBundle?.locale?.let { applicationContextRepository.setLocale(it) }
        }

        return requireNotNull(itemsBundle)
    }

    override fun getStringWidth(string: String): Float {
        glyphLayout.setText(font, string)
        return glyphLayout.width
    }

    override fun getStringHeight(string: String): Float {
        glyphLayout.setText(font, string)
        return glyphLayout.height
    }

    override fun getFont(): BitmapFont = requireNotNull(font)

    override fun initialize() {
        font = BitmapFont(Gdx.files.internal(FONT_FILE_PATH), true)
            .apply {
                data.setScale(.375f)
                setUseIntegerPositions(false)
            }
        loadLocalization()
    }

    override fun dispose() {
        super.dispose()
        font?.dispose()
    }

    private fun loadLocalization() {
        val currentLocale = applicationContextRepository.getLocale()
        Gdx.app.debug(TAG, "Loading localization for locale $currentLocale")
        I18NBundle.setSimpleFormatter(true)
        menuBundle = I18NBundle.createBundle(Gdx.files.internal(BASE_MENU_LOCALIZATION), currentLocale)
        itemsBundle = I18NBundle.createBundle(Gdx.files.internal(BASE_ITEMS_LOCALIZATION), currentLocale)
    }

    companion object {
        private const val TAG = "FontAssetsRepositoryImpl"
        private const val FONT_FILE_PATH = "skin/f77.fnt"
        private const val BASE_MENU_LOCALIZATION = "i18n/CaveDroid_Menu"
        private const val BASE_ITEMS_LOCALIZATION = "i18n/CaveDroid_Items"
        private const val FONT_SCALE = .375f
    }
}
