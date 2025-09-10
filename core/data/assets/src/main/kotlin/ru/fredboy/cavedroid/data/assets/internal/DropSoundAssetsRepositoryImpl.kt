package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import ru.fredboy.cavedroid.domain.assets.repository.DropSoundAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DropSoundAssetsRepositoryImpl @Inject constructor() : DropSoundAssetsRepository() {

    private var dropSounds: List<Sound>? = null

    override fun initialize() {
        dropSounds = Gdx.files.internal(DROP_POP_PATH).list { file ->
            file.extension == "ogg"
        }.map { soundHandle ->
            loadSound(soundHandle)
        }
    }

    override fun getDropPopSound(): Sound? {
        return dropSounds?.takeIf { it.isNotEmpty() }?.random()
    }

    override fun dispose() {
        super.dispose()
        dropSounds = null
    }

    companion object {
        private const val DROP_POP_PATH = "sfx/drop/pop"
    }
}
