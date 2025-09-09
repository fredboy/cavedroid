package ru.fredboy.cavedroid.domain.assets.repository

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import java.util.*

abstract class SoundAssetsRepository : AssetsRepository {

    protected val loadedSounds = LinkedList<Sound>()

    protected fun loadSound(handle: FileHandle): Sound {
        return Gdx.audio.newSound(handle).also { sound ->
            loadedSounds.add(sound)
        }
    }

    override fun dispose() {
        loadedSounds.forEach(Sound::dispose)
        loadedSounds.clear()
    }
}
