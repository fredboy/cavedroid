package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.GdxRuntimeException
import ru.fredboy.cavedroid.domain.assets.repository.BlockActionSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BlockActionSoundAssetsRepositoryImpl @Inject constructor() : BlockActionSoundAssetsRepository() {

    private val soundCache = mutableMapOf<String, List<Sound>>()

    override fun initialize() {
        // no-op
    }

    override fun getBlockActionSound(key: String): Sound? {
        return (
            soundCache[key] ?: try {
                Gdx.files.internal("$BLOCK_ACTION_SOUND_PATH/$key").loadAllSounds()
            } catch (e: GdxRuntimeException) {
                Gdx.app.error(TAG, "Couldn't load sounds for key $key", e)
                emptyList()
            }.also { sounds ->
                soundCache[key] = sounds
            }
            ).let { sounds -> sounds.takeIf { it.isNotEmpty() }?.random() }
    }

    override fun dispose() {
        super.dispose()
        soundCache.clear()
    }

    companion object {
        private const val TAG = "BlockActionSoundAssetsRepositoryImpl"
        private const val BLOCK_ACTION_SOUND_PATH = "sfx/action"
    }
}
