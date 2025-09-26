package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import ru.fredboy.cavedroid.domain.assets.repository.UiSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UiSoundAssetsRepositoryImpl @Inject constructor() : UiSoundAssetsRepository() {

    private var clickSound: Sound? = null

    override fun getClickSound(): Sound {
        return requireNotNull(clickSound) { "[$TAG] clickSound is not set" }
    }

    override fun initialize() {
        clickSound = loadSound(Gdx.files.internal(BUTTON_SOUND))
    }

    override fun dispose() {
        super.dispose()
        clickSound = null
    }

    companion object {
        private const val TAG = "UiSoundAssetsRepositoryImpl"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val BUTTON_SOUND = "sfx/menu/button/button.ogg"
    }
}
