package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.api.GameMessageEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import javax.inject.Inject

@GameScope
class GameMessageController @Inject constructor(
    private val fontTextureAssetsRepository: FontTextureAssetsRepository,
) : GameMessageEvents {

    @Volatile
    private var activeMessage: String? = null

    @Volatile
    private var expiresAtMs: Long = 0L

    val currentMessage: String?
        get() = activeMessage?.takeIf { TimeUtils.millis() < expiresAtMs }

    override fun showLocalized(menuBundleKey: String) {
        activeMessage = fontTextureAssetsRepository.getMenuLocalizationBundle().get(menuBundleKey)
        expiresAtMs = TimeUtils.millis() + DEFAULT_TTL_MS
    }

    companion object {
        private const val DEFAULT_TTL_MS = 2500L
    }
}
